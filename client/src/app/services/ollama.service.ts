import { Injectable, signal } from '@angular/core';
import { Observable, Subject } from 'rxjs';

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp: Date;
  isStreaming?: boolean;
}

export interface OllamaResponse {
  model: string;
  created_at: string;
  response: string;
  done: boolean;
}

export interface OllamaModel {
  name: string;
  size: number;
  digest: string;
}

export interface ProblemContext {
  title: string;
  description: string;
  inputFormat?: string;
  outputFormat?: string;
  constraints?: string;
  examples?: { input: string; output: string; explanation?: string }[];
  difficulty: string;
}

@Injectable({
  providedIn: 'root'
})
export class OllamaService {
  private readonly OLLAMA_API_URL = 'http://localhost:11434/api/generate';
  
  // Preferred models in order of priority (will use first available)
  private readonly PREFERRED_MODELS = [
    'gemma3',
    'llama3.2:1b',
    'llama3.2',
    'llama3.1',
    'llama3',
    'llama2',
    'mistral',
    'codellama',
    'deepseek-coder',
    'qwen2.5-coder',
    'phi',
    'tinyllama'
  ];

  isConnected = signal(false);
  isGenerating = signal(false);
  availableModels = signal<OllamaModel[]>([]);
  currentModel = signal<string>('');
  noModelsInstalled = signal(false);

  constructor() {
    this.checkConnection();
  }

  async checkConnection(): Promise<boolean> {
    try {
      const response = await fetch('http://localhost:11434/api/tags');
      if (response.ok) {
        const data = await response.json();
        const models: OllamaModel[] = data.models || [];
        this.availableModels.set(models);
        
        if (models.length === 0) {
          this.noModelsInstalled.set(true);
          this.isConnected.set(false);
          console.warn('Ollama is running but no models are installed. Run: ollama pull llama3.2:1b');
          return false;
        }
        
        this.noModelsInstalled.set(false);
        
        // Find the best available model from preferred list
        const modelNames = models.map(m => m.name);
        let selectedModel = '';
        
        for (const preferred of this.PREFERRED_MODELS) {
          const found = modelNames.find(m => m.startsWith(preferred));
          if (found) {
            selectedModel = found;
            break;
          }
        }
        
        // If no preferred model found, use the first available
        if (!selectedModel && models.length > 0) {
          selectedModel = models[0].name;
        }
        
        this.currentModel.set(selectedModel);
        this.isConnected.set(true);
        console.log('Ollama connected. Using model:', selectedModel);
        return true;
      }
      this.isConnected.set(false);
      return false;
    } catch {
      this.isConnected.set(false);
      return false;
    }
  }

  buildSystemPrompt(problemContext: ProblemContext | null): string {
    if (!problemContext) {
      return `You are a helpful coding assistant for an Online Judge platform. 
You ONLY answer questions related to competitive programming, algorithms, data structures, and problem-solving techniques.

STRICT RULES:
- ONLY discuss topics related to: algorithms, data structures, coding problems, time/space complexity, debugging code, programming languages used in competitive programming (C++, Java, Python, JavaScript)
- If asked about anything unrelated to programming problems (e.g., general knowledge, personal questions, non-coding topics), politely redirect: "I'm here to help you with programming problems and algorithms. Please ask me about the coding challenges on this platform!"
- Do NOT give away complete solutions directly - guide users to find the answer themselves
- Be concise but thorough when explaining concepts
- Use code examples when helpful for illustration

Stay focused on helping users improve their competitive programming skills.`;
    }

    return `You are an AI coding tutor helping a student solve THIS SPECIFIC programming problem on an Online Judge platform.

PROBLEM CONTEXT:
Title: ${problemContext.title}
Difficulty: ${problemContext.difficulty}
Description: ${problemContext.description}
${problemContext.inputFormat ? `Input Format: ${problemContext.inputFormat}` : ''}
${problemContext.outputFormat ? `Output Format: ${problemContext.outputFormat}` : ''}
${problemContext.constraints ? `Constraints: ${problemContext.constraints}` : ''}
${problemContext.examples?.length ? `Examples:\n${problemContext.examples.map((e, i) => 
  `Example ${i + 1}: Input: ${e.input}, Output: ${e.output}${e.explanation ? `, Explanation: ${e.explanation}` : ''}`
).join('\n')}` : ''}

STRICT RULES:
1. ONLY answer questions directly related to solving THIS specific problem
2. If the user asks about unrelated topics (general knowledge, personal questions, other subjects), politely respond: "I'm here to help you solve this specific problem: '${problemContext.title}'. Please ask questions related to this coding challenge!"
3. Help the student understand the problem without giving away the complete solution directly
4. Guide them through the thought process and algorithm design for THIS problem
5. Provide hints, explain relevant concepts, and help debug their code if they share it
6. If they're stuck, suggest approaches or ask guiding questions specific to THIS problem
7. Explain time and space complexity when relevant to THIS problem
8. Be encouraging and supportive
9. Use code snippets only for illustration, not complete solutions
10. Answer in a clear, structured way using markdown formatting

Remember: Stay strictly focused on helping solve THIS problem: "${problemContext.title}". Redirect any off-topic questions.`;
  }

  generateResponse(
    userMessage: string,
    conversationHistory: ChatMessage[],
    problemContext: ProblemContext | null
  ): Observable<string> {
    const subject = new Subject<string>();

    this.isGenerating.set(true);

    const systemPrompt = this.buildSystemPrompt(problemContext);
    
    // Build conversation context
    const contextMessages = conversationHistory
      .slice(-10) // Last 10 messages for context
      .map(msg => `${msg.role === 'user' ? 'User' : 'Assistant'}: ${msg.content}`)
      .join('\n\n');

    const fullPrompt = contextMessages 
      ? `${contextMessages}\n\nUser: ${userMessage}`
      : userMessage;

    this.streamGenerate(systemPrompt, fullPrompt, subject);

    return subject.asObservable();
  }

  private async streamGenerate(
    systemPrompt: string,
    prompt: string,
    subject: Subject<string>
  ): Promise<void> {
    const model = this.currentModel();
    if (!model) {
      subject.error(new Error('No model available. Please install a model with: ollama pull llama3.2:1b'));
      this.isGenerating.set(false);
      return;
    }

    try {
      const response = await fetch(this.OLLAMA_API_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          model: model,
          prompt: prompt,
          system: systemPrompt,
          stream: true,
          options: {
            temperature: 0.7,
            top_p: 0.9,
            num_predict: 2048,
          }
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error('No response body');
      }

      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();
        
        if (done) {
          break;
        }

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          if (line.trim()) {
            try {
              const json: OllamaResponse = JSON.parse(line);
              if (json.response) {
                subject.next(json.response);
              }
              if (json.done) {
                subject.complete();
                this.isGenerating.set(false);
                return;
              }
            } catch (e) {
              console.warn('Failed to parse JSON line:', line);
            }
          }
        }
      }

      subject.complete();
    } catch (error) {
      console.error('Ollama API error:', error);
      subject.error(error);
    } finally {
      this.isGenerating.set(false);
    }
  }

  stopGeneration(): void {
    this.isGenerating.set(false);
  }
}

