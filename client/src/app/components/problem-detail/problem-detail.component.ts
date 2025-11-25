import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

interface ProblemDetail {
  id: number;
  title: string;
  description: string;
  inputFormat?: string;
  outputFormat?: string;
  constraints?: string;
  difficulty: string;
  timeLimit: number;
  memoryLimit: number;
  tags?: string[];
  examples?: {
    input: string;
    output: string;
    explanation?: string;
  }[];
}

@Component({
  selector: 'app-problem-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <button (click)="goBack()" class="btn-back">← Back to Problems</button>
      </div>

      @if (loading()) {
        <div class="loading">Loading problem...</div>
      } @else if (error()) {
        <div class="error">{{ error() }}</div>
      } @else if (problem()) {
        <div class="problem-layout">
          <div class="problem-panel">
            <div class="problem-header">
              <h1>{{ problem()!.id }}. {{ problem()!.title }}</h1>
              <span class="difficulty" [class]="'difficulty-' + problem()!.difficulty.toLowerCase()">
                {{ formatDifficulty(problem()!.difficulty) }}
              </span>
            </div>

            @if (problem()!.tags && problem()!.tags!.length > 0) {
              <div class="tags">
                @for (tag of problem()!.tags; track tag) {
                  <span class="tag">{{ tag }}</span>
                }
              </div>
            }

            <div class="meta">
              <span>⏱️ Time Limit: {{ problem()!.timeLimit }}ms</span>
              <span>💾 Memory Limit: {{ problem()!.memoryLimit }}MB</span>
            </div>

            <div class="section">
              <h3>Description</h3>
              <div class="content">{{ problem()!.description }}</div>
            </div>

            @if (problem()!.inputFormat) {
              <div class="section">
                <h3>Input Format</h3>
                <div class="content">{{ problem()!.inputFormat }}</div>
              </div>
            }

            @if (problem()!.outputFormat) {
              <div class="section">
                <h3>Output Format</h3>
                <div class="content">{{ problem()!.outputFormat }}</div>
              </div>
            }

            @if (problem()!.constraints) {
              <div class="section">
                <h3>Constraints</h3>
                <div class="content">{{ problem()!.constraints }}</div>
              </div>
            }

            @if (problem()!.examples && problem()!.examples!.length > 0) {
              <div class="section">
                <h3>Examples</h3>
                @for (example of problem()!.examples; track $index) {
                  <div class="example">
                    <div class="example-title">Example {{ $index + 1 }}</div>
                    <div class="example-block">
                      <strong>Input:</strong>
                      <pre>{{ example.input }}</pre>
                    </div>
                    <div class="example-block">
                      <strong>Output:</strong>
                      <pre>{{ example.output }}</pre>
                    </div>
                    @if (example.explanation) {
                      <div class="example-block">
                        <strong>Explanation:</strong>
                        <p>{{ example.explanation }}</p>
                      </div>
                    }
                  </div>
                }
              </div>
            }
          </div>

          <div class="code-panel">
            <div class="panel-header">
              <h3>Submit Solution</h3>
            </div>

            <div class="language-select">
              <label>Language:</label>
              <select [(ngModel)]="selectedLanguage" name="language">
                <option value="JAVA">Java</option>
                <option value="PYTHON">Python</option>
                <option value="CPP">C++</option>
                <option value="C">C</option>
                <option value="JAVASCRIPT">JavaScript</option>
              </select>
            </div>

            <textarea 
              [(ngModel)]="code" 
              placeholder="Write your code here..."
              class="code-editor"
              spellcheck="false"
            ></textarea>

            <div class="submit-actions">
              <button (click)="submitCode()" [disabled]="submitting()" class="btn-submit">
                {{ submitting() ? 'Submitting...' : 'Submit Solution' }}
              </button>
              <button (click)="runCode()" [disabled]="running()" class="btn-run">
                {{ running() ? 'Running...' : 'Run Code' }}
              </button>
            </div>

            @if (submitResult()) {
              <div class="result" [class.success]="submitResult()!.accepted" [class.error]="!submitResult()!.accepted">
                <strong>{{ submitResult()!.verdict }}</strong>
                @if (submitResult()!.message) {
                  <p>{{ submitResult()!.message }}</p>
                }
              </div>
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .container {
      padding: 20px;
      max-width: 1600px;
      margin: 0 auto;
    }

    .header {
      margin-bottom: 20px;
    }

    .btn-back {
      padding: 8px 16px;
      background: white;
      border: 2px solid #333;
      border-radius: 6px;
      cursor: pointer;
      font-size: 14px;
      font-weight: 600;
      transition: all 0.2s;
    }

    .btn-back:hover {
      background: #333;
      color: white;
    }

    .loading, .error {
      text-align: center;
      padding: 40px;
      font-size: 18px;
    }

    .error {
      color: #dc3545;
    }

    .problem-layout {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
    }

    .problem-panel, .code-panel {
      background: white;
      border-radius: 12px;
      padding: 30px;
      border: 2px solid #e0e0e0;
      overflow-y: auto;
      max-height: calc(100vh - 140px);
    }

    .problem-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .problem-header h1 {
      font-size: 28px;
      margin: 0;
      color: #000;
      font-weight: 800;
    }

    .difficulty {
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 13px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .difficulty-easy {
      background: #000;
      color: #90EE90;
    }

    .difficulty-medium {
      background: #000;
      color: #FFD700;
    }

    .difficulty-hard {
      background: #000;
      color: #FF6B6B;
    }

    .tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 20px;
    }

    .tag {
      padding: 6px 12px;
      background: #f5f5f5;
      border-radius: 6px;
      font-size: 13px;
      color: #333;
      border: 1px solid #e0e0e0;
    }

    .meta {
      display: flex;
      gap: 20px;
      margin-bottom: 30px;
      padding: 15px;
      background: #000;
      border-radius: 8px;
      font-size: 14px;
      color: white;
      font-weight: 600;
    }

    .section {
      margin-bottom: 30px;
    }

    .section h3 {
      font-size: 20px;
      margin: 0 0 15px 0;
      color: #000;
      font-weight: 700;
      border-bottom: 3px solid #000;
      padding-bottom: 10px;
    }

    .content {
      line-height: 1.8;
      color: #333;
      white-space: pre-wrap;
    }

    .example {
      margin-bottom: 20px;
      border: 2px solid #e0e0e0;
      border-radius: 8px;
      overflow: hidden;
      transition: all 0.2s;
    }

    .example:hover {
      border-color: #000;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .example-title {
      background: #000;
      color: white;
      padding: 12px 15px;
      font-weight: 700;
      font-size: 15px;
    }

    .example-block {
      padding: 15px;
      border-bottom: 1px solid #f0f0f0;
      background: white;
    }

    .example-block:last-child {
      border-bottom: none;
    }

    .example-block strong {
      color: #000;
      font-weight: 700;
      display: block;
      margin-bottom: 8px;
    }

    .example-block pre {
      background: #f5f5f5;
      padding: 12px;
      border-radius: 6px;
      border: 1px solid #e0e0e0;
      margin: 0;
      overflow-x: auto;
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 14px;
      line-height: 1.6;
      color: #000;
    }

    .example-block p {
      margin: 0;
      color: #333;
      line-height: 1.6;
    }

    .panel-header {
      margin-bottom: 20px;
    }

    .panel-header h3 {
      margin: 0;
      font-size: 22px;
      color: #333;
    }

    .language-select {
      margin-bottom: 15px;
    }

    .language-select label {
      display: block;
      margin-bottom: 8px;
      font-weight: 600;
      color: #333;
    }

    .language-select select {
      width: 100%;
      padding: 10px;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      font-size: 14px;
      cursor: pointer;
    }

    .code-editor {
      width: 100%;
      min-height: 400px;
      padding: 15px;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      font-family: 'Courier New', monospace;
      font-size: 14px;
      resize: vertical;
      margin-bottom: 15px;
    }

    .code-editor:focus {
      outline: none;
      border-color: #667eea;
    }

    .submit-actions {
      display: flex;
      gap: 10px;
      margin-bottom: 15px;
    }

    .btn-submit, .btn-run {
      flex: 1;
      padding: 12px 24px;
      border: none;
      border-radius: 6px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-submit {
      background: #28a745;
      color: white;
    }

    .btn-submit:hover:not(:disabled) {
      background: #218838;
    }

    .btn-run {
      background: #007bff;
      color: white;
    }

    .btn-run:hover:not(:disabled) {
      background: #0056b3;
    }

    .btn-submit:disabled, .btn-run:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .result {
      padding: 15px;
      border-radius: 6px;
      margin-top: 15px;
    }

    .result.success {
      background: #d4edda;
      border: 1px solid #c3e6cb;
      color: #155724;
    }

    .result.error {
      background: #f8d7da;
      border: 1px solid #f5c6cb;
      color: #721c24;
    }

    .result strong {
      display: block;
      margin-bottom: 8px;
      font-size: 16px;
    }

    @media (max-width: 1200px) {
      .problem-layout {
        grid-template-columns: 1fr;
      }

      .problem-panel, .code-panel {
        max-height: none;
      }
    }
  `]
})
export class ProblemDetailComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  problem = signal<ProblemDetail | null>(null);
  loading = signal(false);
  error = signal('');
  
  selectedLanguage = 'JAVA';
  code = '';
  submitting = signal(false);
  running = signal(false);
  submitResult = signal<{accepted: boolean, verdict: string, message?: string} | null>(null);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadProblem(parseInt(id));
    }
  }

  loadProblem(id: number) {
    this.loading.set(true);
    this.http.get<ProblemDetail>(`http://localhost:8085/api/v1/problems/${id}`).subscribe({
      next: (data) => {
        this.problem.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(`Failed to load problem: ${err.statusText}`);
        this.loading.set(false);
      }
    });
  }

  submitCode() {
    if (!this.code.trim()) {
      alert('Please write some code first');
      return;
    }

    this.submitting.set(true);
    this.submitResult.set(null);

    const submission = {
      problemId: this.problem()!.id,
      language: this.selectedLanguage,
      sourceCode: this.code
    };

    this.http.post<any>('http://localhost:8085/api/v1/submissions', submission).subscribe({
      next: (result) => {
        this.submitResult.set({
          accepted: result.verdict === 'ACCEPTED',
          verdict: result.verdict || 'Submitted',
          message: result.message
        });
        this.submitting.set(false);
      },
      error: (err) => {
        this.submitResult.set({
          accepted: false,
          verdict: 'Error',
          message: err.error?.message || 'Failed to submit'
        });
        this.submitting.set(false);
      }
    });
  }

  runCode() {
    if (!this.code.trim()) {
      alert('Please write some code first');
      return;
    }

    this.running.set(true);
    this.submitResult.set(null);

    // Simulate run - adjust to your API
    setTimeout(() => {
      this.submitResult.set({
        accepted: true,
        verdict: 'Sample Test Passed',
        message: 'Your code passed the sample test cases'
      });
      this.running.set(false);
    }, 1500);
  }

  formatDifficulty(diff: string): string {
    return diff.charAt(0) + diff.slice(1).toLowerCase();
  }

  goBack() {
    this.router.navigate(['/problems']);
  }
}
