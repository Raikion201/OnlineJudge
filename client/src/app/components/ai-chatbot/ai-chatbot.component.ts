import { Component, inject, signal, input, effect, ElementRef, ViewChild, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OllamaService, ChatMessage, ProblemContext } from '../../services/ollama.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-ai-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <!-- Floating Chat Button -->
    @if (!isOpen()) {
      <button
        (click)="toggleChat()"
        class="chat-fab"
        [class.pulse]="!hasInteracted()"
      >
        <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
            d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"/>
        </svg>
        <span class="fab-badge">AI</span>
      </button>
    }

    <!-- Chat Panel -->
    @if (isOpen()) {
      <div class="chat-panel">
        <!-- Header -->
        <div class="chat-header">
          <div class="header-left">
            <div class="ai-avatar">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
            </div>
            <div>
              <h3 class="header-title">AI Tutor</h3>
              <span class="header-status" [class.connected]="ollamaService.isConnected()">
                @if (ollamaService.isConnected()) {
                  ● {{ ollamaService.currentModel() }}
                } @else {
                  ○ Offline
                }
              </span>
            </div>
          </div>
          <div class="header-actions">
            <button (click)="clearChat()" class="header-btn" title="Clear chat">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
              </svg>
            </button>
            <button (click)="toggleChat()" class="header-btn" title="Close">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- Connection Warning -->
        @if (!ollamaService.isConnected()) {
          <div class="connection-warning">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
            </svg>
            @if (ollamaService.noModelsInstalled()) {
              <span>No models installed. Run: <code>ollama pull llama3.2:1b</code></span>
            } @else {
              <span>Ollama not detected. Make sure it's running on port 11434.</span>
            }
            <button (click)="ollamaService.checkConnection()" class="retry-btn">Retry</button>
          </div>
        }

        <!-- Messages Container -->
        <div class="messages-container" #messagesContainer>
          <!-- Welcome Message -->
          @if (messages().length === 0) {
            <div class="welcome-message">
              <div class="welcome-icon">🤖</div>
              <h4>Hi! I'm your AI Coding Tutor</h4>
              <p>I can help you understand this problem, debug your code, or explain concepts. Ask me anything!</p>
              
              <div class="quick-prompts">
                <button (click)="sendQuickPrompt('Explain this problem in simple terms')" class="quick-prompt-btn">
                  📖 Explain the problem
                </button>
                <button (click)="sendQuickPrompt('What approach should I use to solve this?')" class="quick-prompt-btn">
                  💡 Suggest an approach
                </button>
                <button (click)="sendQuickPrompt('What are the edge cases I should consider?')" class="quick-prompt-btn">
                  🔍 Find edge cases
                </button>
                <button (click)="sendQuickPrompt('Give me a hint without spoiling the solution')" class="quick-prompt-btn">
                  🎯 Give me a hint
                </button>
              </div>
            </div>
          }

          <!-- Chat Messages -->
          @for (message of messages(); track message.id) {
            <div class="message" [class.user]="message.role === 'user'" [class.assistant]="message.role === 'assistant'">
              <div class="message-avatar">
                @if (message.role === 'user') {
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                  </svg>
                }
                @if (message.role === 'assistant') {
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                  </svg>
                }
              </div>
              <div class="message-content">
                <div class="message-bubble" [innerHTML]="formatMessage(message.content)"></div>
                <span class="message-time">{{ formatTime(message.timestamp) }}</span>
              </div>
            </div>
          }

          <!-- Typing Indicator -->
          @if (ollamaService.isGenerating()) {
            <div class="message assistant">
              <div class="message-avatar">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                </svg>
              </div>
              <div class="message-content">
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          }
        </div>

        <!-- Input Area -->
        <div class="input-area">
          <div class="input-wrapper">
            <textarea
              #inputField
              [(ngModel)]="inputMessage"
              (keydown.enter)="handleEnter($event)"
              placeholder="Ask me about the problem..."
              rows="1"
              [disabled]="ollamaService.isGenerating() || !ollamaService.isConnected()"
            ></textarea>
            <button 
              (click)="sendMessage()" 
              [disabled]="!inputMessage().trim() || ollamaService.isGenerating() || !ollamaService.isConnected()"
              class="send-btn"
            >
              @if (!ollamaService.isGenerating()) {
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/>
                </svg>
              } @else {
                <svg class="w-5 h-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              }
            </button>
          </div>
          <p class="input-hint">Press Enter to send, Shift+Enter for new line</p>
        </div>
      </div>
    }
  `,
  styles: [`
    /* Floating Action Button */
    .chat-fab {
      position: fixed;
      bottom: 24px;
      right: 24px;
      width: 64px;
      height: 64px;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      box-shadow: 0 8px 32px rgba(102, 126, 234, 0.4);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      z-index: 1000;
    }

    .chat-fab:hover {
      transform: scale(1.1);
      box-shadow: 0 12px 40px rgba(102, 126, 234, 0.5);
    }

    .chat-fab.pulse::before {
      content: '';
      position: absolute;
      width: 100%;
      height: 100%;
      border-radius: 50%;
      background: inherit;
      animation: pulse 2s infinite;
      z-index: -1;
    }

    @keyframes pulse {
      0% { transform: scale(1); opacity: 0.8; }
      50% { transform: scale(1.3); opacity: 0; }
      100% { transform: scale(1); opacity: 0; }
    }

    .fab-badge {
      position: absolute;
      top: -4px;
      right: -4px;
      background: #10b981;
      color: white;
      font-size: 10px;
      font-weight: 700;
      padding: 3px 6px;
      border-radius: 8px;
      border: 2px solid white;
    }

    /* Chat Panel */
    .chat-panel {
      position: fixed;
      bottom: 24px;
      right: 24px;
      width: 420px;
      height: 600px;
      max-height: calc(100vh - 48px);
      background: #0f0f1a;
      border-radius: 20px;
      box-shadow: 0 25px 80px rgba(0, 0, 0, 0.5);
      display: flex;
      flex-direction: column;
      overflow: hidden;
      z-index: 1001;
      animation: slideIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      border: 1px solid rgba(255, 255, 255, 0.1);
    }

    @keyframes slideIn {
      from {
        opacity: 0;
        transform: translateY(20px) scale(0.95);
      }
      to {
        opacity: 1;
        transform: translateY(0) scale(1);
      }
    }

    /* Header */
    .chat-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .ai-avatar {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .header-title {
      margin: 0;
      font-size: 16px;
      font-weight: 700;
    }

    .header-status {
      font-size: 12px;
      opacity: 0.8;
    }

    .header-status.connected {
      color: #86efac;
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }

    .header-btn {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: rgba(255, 255, 255, 0.1);
      border: none;
      color: white;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: background 0.2s;
    }

    .header-btn:hover {
      background: rgba(255, 255, 255, 0.2);
    }

    /* Connection Warning */
    .connection-warning {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 16px;
      background: #fef3c7;
      color: #92400e;
      font-size: 13px;
      flex-wrap: wrap;
    }

    .connection-warning code {
      background: #92400e;
      color: white;
      padding: 2px 6px;
      border-radius: 4px;
      font-family: 'Fira Code', monospace;
      font-size: 11px;
    }

    .retry-btn {
      margin-left: auto;
      padding: 4px 12px;
      background: #d97706;
      color: white;
      border: none;
      border-radius: 6px;
      font-size: 12px;
      cursor: pointer;
    }

    /* Messages Container */
    .messages-container {
      flex: 1;
      overflow-y: auto;
      padding: 20px;
      display: flex;
      flex-direction: column;
      gap: 16px;
      background: #0f0f1a;
    }

    .messages-container::-webkit-scrollbar {
      width: 6px;
    }

    .messages-container::-webkit-scrollbar-track {
      background: transparent;
    }

    .messages-container::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 3px;
    }

    /* Welcome Message */
    .welcome-message {
      text-align: center;
      padding: 24px;
      color: rgba(255, 255, 255, 0.9);
    }

    .welcome-icon {
      font-size: 48px;
      margin-bottom: 16px;
    }

    .welcome-message h4 {
      margin: 0 0 8px 0;
      font-size: 18px;
      font-weight: 700;
      background: linear-gradient(135deg, #667eea 0%, #a78bfa 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .welcome-message p {
      margin: 0 0 24px 0;
      font-size: 14px;
      color: rgba(255, 255, 255, 0.6);
      line-height: 1.5;
    }

    .quick-prompts {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .quick-prompt-btn {
      padding: 12px 16px;
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
      color: rgba(255, 255, 255, 0.8);
      font-size: 13px;
      cursor: pointer;
      text-align: left;
      transition: all 0.2s;
    }

    .quick-prompt-btn:hover {
      background: rgba(102, 126, 234, 0.2);
      border-color: rgba(102, 126, 234, 0.5);
      color: white;
    }

    /* Messages */
    .message {
      display: flex;
      gap: 12px;
      max-width: 90%;
    }

    .message.user {
      align-self: flex-end;
      flex-direction: row-reverse;
    }

    .message-avatar {
      width: 32px;
      height: 32px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .message.user .message-avatar {
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
      color: white;
    }

    .message.assistant .message-avatar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .message-content {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .message.user .message-content {
      align-items: flex-end;
    }

    .message-bubble {
      padding: 12px 16px;
      border-radius: 16px;
      font-size: 14px;
      line-height: 1.6;
      word-break: break-word;
    }

    .message.user .message-bubble {
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
      color: white;
      border-bottom-right-radius: 4px;
    }

    .message.assistant .message-bubble {
      background: rgba(255, 255, 255, 0.08);
      color: rgba(255, 255, 255, 0.9);
      border-bottom-left-radius: 4px;
    }

    .message-bubble :global(code) {
      background: rgba(0, 0, 0, 0.3);
      padding: 2px 6px;
      border-radius: 4px;
      font-family: 'Fira Code', monospace;
      font-size: 13px;
    }

    .message-bubble :global(pre) {
      background: rgba(0, 0, 0, 0.4);
      padding: 12px;
      border-radius: 8px;
      overflow-x: auto;
      margin: 8px 0;
    }

    .message-bubble :global(pre code) {
      background: transparent;
      padding: 0;
    }

    .message-time {
      font-size: 11px;
      color: rgba(255, 255, 255, 0.4);
      padding: 0 4px;
    }

    /* Typing Indicator */
    .typing-indicator {
      display: flex;
      gap: 4px;
      padding: 16px;
    }

    .typing-indicator span {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.4);
      animation: typing 1.4s infinite;
    }

    .typing-indicator span:nth-child(2) {
      animation-delay: 0.2s;
    }

    .typing-indicator span:nth-child(3) {
      animation-delay: 0.4s;
    }

    @keyframes typing {
      0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
      30% { transform: translateY(-8px); opacity: 1; }
    }

    /* Input Area */
    .input-area {
      padding: 16px 20px;
      background: rgba(255, 255, 255, 0.03);
      border-top: 1px solid rgba(255, 255, 255, 0.08);
    }

    .input-wrapper {
      display: flex;
      gap: 12px;
      align-items: flex-end;
    }

    .input-wrapper textarea {
      flex: 1;
      min-height: 44px;
      max-height: 120px;
      padding: 12px 16px;
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 14px;
      color: white;
      font-size: 14px;
      font-family: inherit;
      resize: none;
      outline: none;
      transition: all 0.2s;
    }

    .input-wrapper textarea::placeholder {
      color: rgba(255, 255, 255, 0.4);
    }

    .input-wrapper textarea:focus {
      border-color: #667eea;
      background: rgba(102, 126, 234, 0.1);
    }

    .input-wrapper textarea:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .send-btn {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      color: white;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;
      flex-shrink: 0;
    }

    .send-btn:hover:not(:disabled) {
      transform: scale(1.05);
      box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
    }

    .send-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .input-hint {
      margin: 8px 0 0 0;
      font-size: 11px;
      color: rgba(255, 255, 255, 0.3);
      text-align: center;
    }

    /* Utilities */
    .w-5 { width: 1.25rem; }
    .h-5 { height: 1.25rem; }
    .w-7 { width: 1.75rem; }
    .h-7 { height: 1.75rem; }

    .animate-spin {
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }

    /* Responsive */
    @media (max-width: 480px) {
      .chat-panel {
        width: calc(100vw - 32px);
        height: calc(100vh - 100px);
        bottom: 16px;
        right: 16px;
      }
    }
  `]
})
export class AiChatbotComponent implements OnDestroy {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;
  @ViewChild('inputField') inputField!: ElementRef;

  ollamaService = inject(OllamaService);

  // Input for problem context from parent component
  problemContext = input<ProblemContext | null>(null);

  isOpen = signal(false);
  hasInteracted = signal(false);
  messages = signal<ChatMessage[]>([]);
  inputMessage = signal('');

  private currentResponseSubscription?: Subscription;

  constructor() {
    // Auto-scroll when messages change
    effect(() => {
      this.messages();
      setTimeout(() => this.scrollToBottom(), 100);
    });
  }

  ngOnDestroy() {
    this.currentResponseSubscription?.unsubscribe();
  }

  toggleChat() {
    this.isOpen.update(v => !v);
    this.hasInteracted.set(true);
    
    if (this.isOpen()) {
      setTimeout(() => {
        this.inputField?.nativeElement?.focus();
      }, 100);
    }
  }

  clearChat() {
    this.messages.set([]);
    this.currentResponseSubscription?.unsubscribe();
    this.ollamaService.stopGeneration();
  }

  handleEnter(event: Event) {
    const keyEvent = event as KeyboardEvent;
    if (!keyEvent.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  sendQuickPrompt(prompt: string) {
    this.inputMessage.set(prompt);
    this.sendMessage();
  }

  sendMessage() {
    const content = this.inputMessage().trim();
    if (!content || this.ollamaService.isGenerating() || !this.ollamaService.isConnected()) {
      return;
    }

    // Add user message
    const userMessage: ChatMessage = {
      id: crypto.randomUUID(),
      role: 'user',
      content: content,
      timestamp: new Date()
    };
    this.messages.update(msgs => [...msgs, userMessage]);
    this.inputMessage.set('');

    // Create placeholder for assistant response
    const assistantMessage: ChatMessage = {
      id: crypto.randomUUID(),
      role: 'assistant',
      content: '',
      timestamp: new Date(),
      isStreaming: true
    };
    this.messages.update(msgs => [...msgs, assistantMessage]);

    // Subscribe to streaming response
    this.currentResponseSubscription?.unsubscribe();
    this.currentResponseSubscription = this.ollamaService.generateResponse(
      content,
      this.messages().slice(0, -1), // Exclude the empty assistant message
      this.problemContext()
    ).subscribe({
      next: (chunk) => {
        this.messages.update(msgs => {
          const updated = [...msgs];
          const lastMsg = updated[updated.length - 1];
          if (lastMsg && lastMsg.role === 'assistant') {
            lastMsg.content += chunk;
          }
          return updated;
        });
      },
      error: (error) => {
        console.error('Error generating response:', error);
        this.messages.update(msgs => {
          const updated = [...msgs];
          const lastMsg = updated[updated.length - 1];
          if (lastMsg && lastMsg.role === 'assistant') {
            lastMsg.content = '❌ Sorry, I encountered an error. Please make sure Ollama is running and try again.';
            lastMsg.isStreaming = false;
          }
          return updated;
        });
      },
      complete: () => {
        this.messages.update(msgs => {
          const updated = [...msgs];
          const lastMsg = updated[updated.length - 1];
          if (lastMsg && lastMsg.role === 'assistant') {
            lastMsg.isStreaming = false;
          }
          return updated;
        });
      }
    });
  }

  formatMessage(content: string): string {
    // Simple markdown-like formatting
    return content
      // Code blocks
      .replace(/```(\w+)?\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
      // Inline code
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      // Bold
      .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
      // Italic
      .replace(/\*([^*]+)\*/g, '<em>$1</em>')
      // Line breaks
      .replace(/\n/g, '<br>');
  }

  formatTime(date: Date): string {
    return date.toLocaleTimeString('en-US', { 
      hour: 'numeric', 
      minute: '2-digit',
      hour12: true 
    });
  }

  private scrollToBottom() {
    if (this.messagesContainer) {
      const container = this.messagesContainer.nativeElement;
      container.scrollTop = container.scrollHeight;
    }
  }
}
