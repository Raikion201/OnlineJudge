import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface Example {
  id?: number;
  input: string;
  output: string;
  explanation?: string;
  ordering: number;
}

interface TestCase {
  id?: number;
  input: string;
  expectedOutput: string;
  isSample: boolean;
  points: number;
  timeLimit?: number;
  memoryLimit?: number;
  ordering: number;
}

@Component({
  selector: 'app-admin-testcases',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page">
      <div class="content">
        <button (click)="goToProblems()" class="btn-back">← Back to Problems</button>
        <h1 class="page-title">Test Cases & Examples - Problem #{{ problemId() }}</h1>

        <!-- Examples Section -->
        <div class="section">
          <h2>Examples (Visible to Users)</h2>
          
          <div class="form-card">
            <h3>{{ editingExampleId() ? 'Edit Example' : 'Add New Example' }}</h3>
            <form (ngSubmit)="saveExample()">
              <div class="form-group">
                <label>Input *</label>
                <textarea [(ngModel)]="exampleForm.input" name="input" rows="3" required></textarea>
              </div>

              <div class="form-group">
                <label>Output *</label>
                <textarea [(ngModel)]="exampleForm.output" name="output" rows="3" required></textarea>
              </div>

              <div class="form-group">
                <label>Explanation</label>
                <textarea [(ngModel)]="exampleForm.explanation" name="explanation" rows="2"></textarea>
              </div>

              <div class="form-group">
                <label>Order</label>
                <input type="number" [(ngModel)]="exampleForm.ordering" name="ordering" min="0">
              </div>

              @if (exampleMessage()) {
                <div class="message" [class.error]="isExampleError()">{{ exampleMessage() }}</div>
              }

              <div class="form-actions">
                <button type="submit" class="btn-primary">
                  {{ editingExampleId() ? 'Update' : 'Add' }} Example
                </button>
                @if (editingExampleId()) {
                  <button type="button" (click)="cancelEditExample()" class="btn-secondary">Cancel</button>
                }
              </div>
            </form>
          </div>

          <div class="list-card">
            @if (loadingExamples()) {
              <p>Loading examples...</p>
            } @else if (examples().length === 0) {
              <p class="empty">No examples added yet.</p>
            } @else {
              @for (example of examples(); track example.id) {
                <div class="item-card">
                  <div class="item-header">
                    <strong>Example {{ example.ordering + 1 }}</strong>
                    <div class="item-actions">
                      <button (click)="editExample(example)" class="btn-sm">Edit</button>
                      <button (click)="deleteExample(example.id!)" class="btn-sm btn-delete">Delete</button>
                    </div>
                  </div>
                  <div class="item-content">
                    <div class="code-block">
                      <strong>Input:</strong>
                      <pre>{{ example.input }}</pre>
                    </div>
                    <div class="code-block">
                      <strong>Output:</strong>
                      <pre>{{ example.output }}</pre>
                    </div>
                    @if (example.explanation) {
                      <div class="explanation">
                        <strong>Explanation:</strong> {{ example.explanation }}
                      </div>
                    }
                  </div>
                </div>
              }
            }
          </div>
        </div>

        <!-- Test Cases Section -->
        <div class="section">
          <h2>Test Cases (Hidden, for Judging)</h2>
          
          <div class="form-card">
            <h3>{{ editingTestCaseId() ? 'Edit Test Case' : 'Add New Test Case' }}</h3>
            <form (ngSubmit)="saveTestCase()">
              <div class="form-group">
                <label>Input *</label>
                <textarea [(ngModel)]="testCaseForm.input" name="input" rows="3" required></textarea>
              </div>

              <div class="form-group">
                <label>Expected Output *</label>
                <textarea [(ngModel)]="testCaseForm.expectedOutput" name="expectedOutput" rows="3" required></textarea>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>
                    <input type="checkbox" [(ngModel)]="testCaseForm.isSample" name="isSample">
                    Is Sample (visible to users)
                  </label>
                </div>

                <div class="form-group">
                  <label>Points</label>
                  <input type="number" [(ngModel)]="testCaseForm.points" name="points" min="0">
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>Time Limit (ms)</label>
                  <input type="number" [(ngModel)]="testCaseForm.timeLimit" name="timeLimit" min="0">
                </div>

                <div class="form-group">
                  <label>Memory Limit (MB)</label>
                  <input type="number" [(ngModel)]="testCaseForm.memoryLimit" name="memoryLimit" min="0">
                </div>

                <div class="form-group">
                  <label>Order</label>
                  <input type="number" [(ngModel)]="testCaseForm.ordering" name="ordering" min="0">
                </div>
              </div>

              @if (testCaseMessage()) {
                <div class="message" [class.error]="isTestCaseError()">{{ testCaseMessage() }}</div>
              }

              <div class="form-actions">
                <button type="submit" class="btn-primary">
                  {{ editingTestCaseId() ? 'Update' : 'Add' }} Test Case
                </button>
                @if (editingTestCaseId()) {
                  <button type="button" (click)="cancelEditTestCase()" class="btn-secondary">Cancel</button>
                }
              </div>
            </form>
          </div>

          <div class="list-card">
            @if (loadingTestCases()) {
              <p>Loading test cases...</p>
            } @else if (testCases().length === 0) {
              <p class="empty">No test cases added yet.</p>
            } @else {
              @for (testCase of testCases(); track testCase.id) {
                <div class="item-card">
                  <div class="item-header">
                    <strong>Test Case {{ testCase.ordering + 1 }}</strong>
                    <div class="badges">
                      @if (testCase.isSample) {
                        <span class="badge badge-sample">Sample</span>
                      }
                      <span class="badge badge-points">{{ testCase.points }} pts</span>
                    </div>
                    <div class="item-actions">
                      <button (click)="editTestCase(testCase)" class="btn-sm">Edit</button>
                      <button (click)="deleteTestCase(testCase.id!)" class="btn-sm btn-delete">Delete</button>
                    </div>
                  </div>
                  <div class="item-content">
                    <div class="code-block">
                      <strong>Input:</strong>
                      <pre>{{ testCase.input }}</pre>
                    </div>
                    <div class="code-block">
                      <strong>Expected Output:</strong>
                      <pre>{{ testCase.expectedOutput }}</pre>
                    </div>
                    @if (testCase.timeLimit || testCase.memoryLimit) {
                      <div class="limits">
                        @if (testCase.timeLimit) {
                          <span>⏱️ {{ testCase.timeLimit }}ms</span>
                        }
                        @if (testCase.memoryLimit) {
                          <span>💾 {{ testCase.memoryLimit }}MB</span>
                        }
                      </div>
                    }
                  </div>
                </div>
              }
            }
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page {
      min-height: 100vh;
      background: #ffffff;
    }

    .btn-back {
      background: transparent;
      border: 2px solid #000;
      color: #000;
      padding: 0.5rem 1rem;
      cursor: pointer;
      font-size: 14px;
      font-weight: 600;
      border-radius: 6px;
      transition: all 0.2s;
      margin-bottom: 1rem;
    }

    .btn-back:hover {
      background: #000;
      color: white;
    }

    .content {
      max-width: 1200px;
      margin: 0 auto;
      padding: 3rem 2rem;
    }

    .page-title {
      margin: 0 0 2rem 0;
      font-size: 2rem;
      color: #000;
      font-weight: 800;
    }

    .section {
      margin-bottom: 4rem;
    }

    .section h2 {
      margin: 0 0 1.5rem 0;
      font-size: 1.5rem;
      color: #000;
      font-weight: 700;
      border-bottom: 3px solid #000;
      padding-bottom: 0.5rem;
    }

    .form-card {
      background: white;
      padding: 2rem;
      border: 2px solid #e0e0e0;
      border-radius: 12px;
      margin-bottom: 2rem;
    }

    .form-card h3 {
      margin: 0 0 1.5rem 0;
      font-size: 1.2rem;
      color: #000;
    }

    .form-group {
      margin-bottom: 1.5rem;
    }

    .form-group label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 600;
      color: #000;
    }

    .form-group input[type="text"],
    .form-group input[type="number"],
    .form-group textarea,
    .form-group select {
      width: 100%;
      padding: 0.75rem;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      font-size: 14px;
      font-family: inherit;
    }

    .form-group input[type="checkbox"] {
      margin-right: 0.5rem;
    }

    .form-group textarea {
      font-family: 'Consolas', 'Monaco', monospace;
      resize: vertical;
    }

    .form-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
    }

    .form-actions {
      display: flex;
      gap: 1rem;
      margin-top: 1.5rem;
    }

    .btn-primary {
      background: #000;
      color: white;
      border: 2px solid #000;
      padding: 0.75rem 1.5rem;
      font-weight: 600;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-primary:hover {
      background: #333;
      border-color: #333;
    }

    .btn-secondary {
      background: white;
      color: #000;
      border: 2px solid #000;
      padding: 0.75rem 1.5rem;
      font-weight: 600;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-secondary:hover {
      background: #f5f5f5;
    }

    .list-card {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .item-card {
      background: white;
      border: 2px solid #e0e0e0;
      border-radius: 8px;
      overflow: hidden;
      transition: all 0.2s;
    }

    .item-card:hover {
      border-color: #000;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .item-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 1rem;
      background: #f5f5f5;
      border-bottom: 1px solid #e0e0e0;
    }

    .item-actions {
      display: flex;
      gap: 0.5rem;
    }

    .btn-sm {
      padding: 0.4rem 0.8rem;
      font-size: 12px;
      border: 1px solid #000;
      background: white;
      color: #000;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.2s;
    }

    .btn-sm:hover {
      background: #000;
      color: white;
    }

    .btn-delete {
      border-color: #dc3545;
      color: #dc3545;
    }

    .btn-delete:hover {
      background: #dc3545;
      color: white;
    }

    .item-content {
      padding: 1rem;
    }

    .code-block {
      margin-bottom: 1rem;
    }

    .code-block strong {
      display: block;
      margin-bottom: 0.5rem;
      color: #000;
      font-weight: 700;
    }

    .code-block pre {
      background: #f5f5f5;
      padding: 0.75rem;
      border-radius: 4px;
      border: 1px solid #e0e0e0;
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 13px;
      line-height: 1.5;
      overflow-x: auto;
      margin: 0;
    }

    .explanation {
      padding: 0.75rem;
      background: #f9f9f9;
      border-left: 3px solid #000;
      margin-top: 1rem;
    }

    .badges {
      display: flex;
      gap: 0.5rem;
    }

    .badge {
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      font-size: 11px;
      font-weight: 700;
      text-transform: uppercase;
    }

    .badge-sample {
      background: #000;
      color: #90EE90;
    }

    .badge-points {
      background: #000;
      color: #FFD700;
    }

    .limits {
      display: flex;
      gap: 1rem;
      padding: 0.5rem;
      background: #f5f5f5;
      border-radius: 4px;
      margin-top: 0.5rem;
      font-size: 13px;
      color: #666;
    }

    .message {
      padding: 0.75rem;
      border-radius: 6px;
      margin: 1rem 0;
      background: #d4edda;
      color: #155724;
      border: 1px solid #c3e6cb;
    }

    .message.error {
      background: #f8d7da;
      color: #721c24;
      border-color: #f5c6cb;
    }

    .empty {
      text-align: center;
      padding: 2rem;
      color: #666;
    }
  `]
})
export class AdminTestCasesComponent implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);

  problemId = signal<number>(0);
  
  // Examples
  examples = signal<Example[]>([]);
  loadingExamples = signal(false);
  editingExampleId = signal<number | null>(null);
  exampleMessage = signal('');
  isExampleError = signal(false);
  
  exampleForm: Example = {
    input: '',
    output: '',
    explanation: '',
    ordering: 0
  };

  // Test Cases
  testCases = signal<TestCase[]>([]);
  loadingTestCases = signal(false);
  editingTestCaseId = signal<number | null>(null);
  testCaseMessage = signal('');
  isTestCaseError = signal(false);
  
  testCaseForm: TestCase = {
    input: '',
    expectedOutput: '',
    isSample: false,
    points: 10,
    timeLimit: undefined,
    memoryLimit: undefined,
    ordering: 0
  };

  ngOnInit() {
    this.route.params.subscribe(params => {
      const id = +params['id'];
      if (id) {
        this.problemId.set(id);
        this.loadExamples();
        this.loadTestCases();
      }
    });
  }

  // Examples Methods
  loadExamples() {
    this.loadingExamples.set(true);
    this.http.get<Example[]>(`http://localhost:8087/api/v1/problems/${this.problemId()}/examples`)
      .subscribe({
        next: (data) => {
          this.examples.set(data);
          this.loadingExamples.set(false);
        },
        error: (err) => {
          console.error('Error loading examples:', err);
          this.loadingExamples.set(false);
        }
      });
  }

  saveExample() {
    const url = this.editingExampleId()
      ? `http://localhost:8087/api/v1/problems/${this.problemId()}/examples/${this.editingExampleId()}`
      : `http://localhost:8087/api/v1/problems/${this.problemId()}/examples`;
    
    const method = this.editingExampleId() ? 'put' : 'post';

    this.http.request(method, url, { body: this.exampleForm }).subscribe({
      next: () => {
        this.exampleMessage.set(`Example ${this.editingExampleId() ? 'updated' : 'added'} successfully`);
        this.isExampleError.set(false);
        this.loadExamples();
        this.resetExampleForm();
      },
      error: (err) => {
        this.exampleMessage.set('Failed to save example');
        this.isExampleError.set(true);
        console.error(err);
      }
    });
  }

  editExample(example: Example) {
    this.exampleForm = { ...example };
    this.editingExampleId.set(example.id!);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  deleteExample(id: number) {
    if (!confirm('Are you sure?')) return;
    
    this.http.delete(`http://localhost:8087/api/v1/problems/${this.problemId()}/examples/${id}`)
      .subscribe({
        next: () => {
          this.loadExamples();
        },
        error: (err) => console.error(err)
      });
  }

  cancelEditExample() {
    this.resetExampleForm();
  }

  resetExampleForm() {
    this.exampleForm = {
      input: '',
      output: '',
      explanation: '',
      ordering: 0
    };
    this.editingExampleId.set(null);
  }

  // Test Cases Methods
  loadTestCases() {
    this.loadingTestCases.set(true);
    this.http.get<TestCase[]>(`http://localhost:8087/api/v1/problems/${this.problemId()}/testcases`)
      .subscribe({
        next: (data) => {
          this.testCases.set(data);
          this.loadingTestCases.set(false);
        },
        error: (err) => {
          console.error('Error loading test cases:', err);
          this.loadingTestCases.set(false);
        }
      });
  }

  saveTestCase() {
    const url = this.editingTestCaseId()
      ? `http://localhost:8087/api/v1/problems/${this.problemId()}/testcases/${this.editingTestCaseId()}`
      : `http://localhost:8087/api/v1/problems/${this.problemId()}/testcases`;
    
    const method = this.editingTestCaseId() ? 'put' : 'post';

    this.http.request(method, url, { body: this.testCaseForm }).subscribe({
      next: () => {
        this.testCaseMessage.set(`Test case ${this.editingTestCaseId() ? 'updated' : 'added'} successfully`);
        this.isTestCaseError.set(false);
        this.loadTestCases();
        this.resetTestCaseForm();
      },
      error: (err) => {
        this.testCaseMessage.set('Failed to save test case');
        this.isTestCaseError.set(true);
        console.error(err);
      }
    });
  }

  editTestCase(testCase: TestCase) {
    this.testCaseForm = { ...testCase };
    this.editingTestCaseId.set(testCase.id!);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  deleteTestCase(id: number) {
    if (!confirm('Are you sure?')) return;
    
    this.http.delete(`http://localhost:8087/api/v1/problems/${this.problemId()}/testcases/${id}`)
      .subscribe({
        next: () => {
          this.loadTestCases();
        },
        error: (err) => console.error(err)
      });
  }

  cancelEditTestCase() {
    this.resetTestCaseForm();
  }

  resetTestCaseForm() {
    this.testCaseForm = {
      input: '',
      expectedOutput: '',
      isSample: false,
      points: 10,
      timeLimit: undefined,
      memoryLimit: undefined,
      ordering: 0
    };
    this.editingTestCaseId.set(null);
  }

  goToProblems() {
    this.router.navigate(['/admin/problems']);
  }
}
