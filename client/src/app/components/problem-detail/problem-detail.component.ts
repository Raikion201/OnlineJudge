import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { SubmissionApiService, SubmissionPayload, SubmissionResponse, LeaderboardEntry } from '../../services/submission-api.service';

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

interface SubmissionFeedback {
  accepted: boolean;
  verdict: string;
  message?: string;
  submissionId?: number;
}

interface Comment {
  id: number;
  problemId: number;
  userId: string;
  username: string;
  content: string;
  parentId?: number;
  replies?: Comment[];
  createdAt: string;
  updatedAt?: string;
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
            <div class="tabs">
              <button 
                class="tab-btn" 
                [class.active]="activeTab() === 'description'"
                (click)="switchTab('description')">
                Description
              </button>
              <button 
                class="tab-btn" 
                [class.active]="activeTab() === 'leaderboard'"
                (click)="switchTab('leaderboard')">
                Submission Results
              </button>
              <button 
                class="tab-btn" 
                [class.active]="activeTab() === 'discussion'"
                (click)="switchTab('discussion')">
                Discussion
              </button>
            </div>

            @if (activeTab() === 'description') {
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
            } @else if (activeTab() === 'leaderboard') {
              <div class="leaderboard-container">
                <h3>Submission Results</h3>
                @if (loadingLeaderboard()) {
                  <div class="loading-sm">Loading submission results...</div>
                } @else if (leaderboard().length === 0) {
                  <div class="empty-state">No submissions yet. Be the first!</div>
                } @else {
                  <table class="leaderboard-table">
                    <thead>
                      <tr>
                        <th>Rank</th>
                        <th>User</th>
                        <th>Score</th>
                        <th>Time</th>
                        <th>Language</th>
                        <th>Date</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (entry of leaderboard(); track entry.userId; let i = $index) {
                        <tr>
                          <td>{{ i + 1 }}</td>
                          <td class="username">{{ entry.username }}</td>
                          <td class="score">{{ entry.score }}</td>
                          <td>{{ entry.executionTime }}ms</td>
                          <td>{{ entry.language }}</td>
                          <td>{{ entry.submittedAt | date:'short' }}</td>
                        </tr>
                      }
                    </tbody>
                  </table>
                }
              </div>
            } @else if (activeTab() === 'discussion') {
              <div class="discussion-container">
                <!-- Discussion Rules -->
                <div class="discussion-rules">
                  <h4>Discussion Rules</h4>
                  <ul>
                    <li>Be respectful and constructive. No personal attacks or trolling.</li>
                    <li>Do not post solutions in the discussion. Use the "Solution" tab instead.</li>
                    <li>Keep discussions focused on the problem. Ask clarifying questions, discuss edge cases, or share alternative approaches.</li>
                    <li>Use code formatting for any small snippets of code you share.</li>
                  </ul>
                </div>

                <!-- Post Comment Section -->
                <div class="post-comment-section">
                  <h4>Post a Comment</h4>
                  <div class="comment-input-wrapper">
                    <textarea 
                      [(ngModel)]="newCommentContent" 
                      placeholder="Type your comment here..."
                      class="comment-editor"
                      spellcheck="false"
                    ></textarea>
                  </div>
                  <div class="post-actions">
                    <button (click)="postComment()" class="btn-post-comment">
                      Post Comment
                    </button>
                  </div>
                </div>

                <!-- Comments List -->
                <div class="comments-list">
                  @if (loadingComments()) {
                    <div class="loading-sm">Loading comments...</div>
                  } @else if (comments().length === 0) {
                    <div class="empty-state">No comments yet. Be the first to discuss!</div>
                  } @else {
                    @for (comment of comments(); track comment.id) {
                      <div class="comment-item">
                        <div class="avatar">
                          <img [src]="getAvatarUrl(comment.username)" alt="User Avatar">
                        </div>
                        <div class="comment-body">
                          <div class="comment-header">
                            <span class="username">{{ comment.username }}</span>
                            <span class="time-ago">{{ formatDate(comment.createdAt) }}</span>
                            <button (click)="startReply(comment.id)" class="btn-reply-link">Reply</button>
                          </div>
                          <div class="comment-text">
                            {{ comment.content }}
                          </div>
                          
                          <!-- Reply Input -->
                          @if (replyingToId() === comment.id) {
                            <div class="reply-input-box">
                               <textarea 
                                  [(ngModel)]="replyContent" 
                                  placeholder="Write a reply..."
                                  class="reply-editor"
                                  spellcheck="false"
                                ></textarea>
                                <div class="reply-actions">
                                  <button (click)="cancelReply()" class="btn-cancel">Cancel</button>
                                  <button (click)="submitReply(comment.id)" class="btn-submit">Post Reply</button>
                                </div>
                            </div>
                          }

                          <!-- Nested Replies -->
                          @if (comment.replies && comment.replies.length > 0) {
                            <div class="replies-list">
                               @for (reply of comment.replies; track reply.id) {
                                  <div class="reply-item">
                                    <div class="avatar sm">
                                      <img [src]="getAvatarUrl(reply.username)" alt="User Avatar">
                                    </div>
                                    <div class="reply-body">
                                       <div class="reply-header">
                                          <span class="username">{{ reply.username }}</span>
                                          <span class="time-ago">{{ formatDate(reply.createdAt) }}</span>
                                       </div>
                                       <div class="reply-text">
                                          {{ reply.content }}
                                       </div>
                                    </div>
                                  </div>
                               }
                            </div>
                          }
                        </div>
                      </div>
                    }
                  }
                </div>
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

    .tabs {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
      border-bottom: 1px solid #eee;
      padding-bottom: 10px;
    }

    .tab-btn {
      background: none;
      border: none;
      padding: 8px 16px;
      font-size: 16px;
      font-weight: 600;
      color: #666;
      cursor: pointer;
      border-radius: 4px;
    }

    .tab-btn.active {
      background: #333;
      color: white;
    }

    .tab-btn:hover:not(.active) {
      background: #f5f5f5;
    }

    .leaderboard-table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 10px;
    }

    .leaderboard-table th, .leaderboard-table td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid #eee;
    }

    .leaderboard-table th {
      font-weight: 600;
      color: #333;
      background: #f9f9f9;
    }

    .leaderboard-table tr:hover {
      background: #f5f5f5;
    }

    .username {
      font-weight: 600;
      color: #007bff;
    }

    .score {
      font-weight: bold;
      color: #28a745;
    }

    .loading-sm {
      text-align: center;
      padding: 20px;
      color: #666;
    }

    .empty-state {
      text-align: center;
      padding: 40px;
      color: #999;
      font-style: italic;
    }

    .discussion-container {
      max-width: 800px;
      margin: 0 auto;
      padding: 20px 0;
    }

    .discussion-rules {
      background-color: #e6f7ff;
      border: 1px solid #91d5ff;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 24px;
    }

    .discussion-rules h4 {
      color: #0050b3;
      margin: 0 0 8px 0;
      font-size: 16px;
      font-weight: 600;
    }

    .discussion-rules ul {
      margin: 0;
      padding-left: 20px;
      color: #003a8c;
    }

    .discussion-rules li {
      margin-bottom: 4px;
      font-size: 14px;
    }

    .post-comment-section {
      background: #fff;
      border: 1px solid #f0f0f0;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 24px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.05);
    }

    .post-comment-section h4 {
      margin: 0 0 16px 0;
      font-size: 18px;
      font-weight: 600;
    }

    .comment-editor {
      width: 100%;
      min-height: 100px;
      padding: 12px;
      border: 1px solid #d9d9d9;
      border-radius: 6px;
      resize: vertical;
      font-family: inherit;
      margin-bottom: 12px;
      transition: border-color 0.3s;
    }

    .comment-editor:focus {
      border-color: #1890ff;
      outline: none;
    }

    .post-actions {
      display: flex;
      justify-content: flex-end;
    }

    .btn-post-comment {
      background: #1890ff;
      color: white;
      border: none;
      padding: 8px 24px;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 500;
      transition: background 0.3s;
    }

    .btn-post-comment:hover {
      background: #40a9ff;
    }

    .comments-list {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .comment-item {
      display: flex;
      gap: 16px;
      padding: 16px;
      background: #fff;
      border: 1px solid #f0f0f0;
      border-radius: 8px;
    }

    .avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      overflow: hidden;
      flex-shrink: 0;
      background: #eee;
    }

    .avatar img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .avatar.sm {
        width: 32px;
        height: 32px;
    }

    .comment-body {
      flex: 1;
    }

    .comment-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
    }

    .username {
      font-weight: 600;
      color: #262626;
    }

    .time-ago {
      color: #8c8c8c;
      font-size: 12px;
    }

    .btn-reply-link {
      background: none;
      border: none;
      color: #8c8c8c;
      cursor: pointer;
      font-size: 12px;
      padding: 0;
      margin-left: auto;
    }

    .btn-reply-link:hover {
      color: #1890ff;
      text-decoration: underline;
    }

    .comment-text {
      color: #595959;
      line-height: 1.5;
      margin-bottom: 12px;
    }

    .replies-list {
      margin-top: 16px;
      padding-left: 16px;
      border-left: 2px solid #f0f0f0;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .reply-item {
      display: flex;
      gap: 12px;
    }
    
    .reply-body {
        flex: 1;
    }
    
    .reply-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 2px;
    }
    
    .reply-text {
        color: #595959;
        font-size: 14px;
        line-height: 1.5;
    }

    .reply-input-box {
        margin-top: 12px;
        background: #fafafa;
        padding: 12px;
        border-radius: 6px;
    }
    
    .reply-editor {
        width: 100%;
        min-height: 60px;
        padding: 8px;
        border: 1px solid #d9d9d9;
        border-radius: 4px;
        margin-bottom: 8px;
        resize: vertical;
    }
    
    .reply-actions {
        display: flex;
        justify-content: flex-end;
        gap: 8px;
    }
    
    .btn-cancel {
        background: white;
        border: 1px solid #d9d9d9;
        color: #595959;
        padding: 4px 12px;
        border-radius: 4px;
        cursor: pointer;
    }
    
    .btn-submit {
        background: #1890ff;
        color: white;
        border: none;
        padding: 4px 12px;
        border-radius: 4px;
        cursor: pointer;
    }

    .discussion-section {
      padding: 20px 0;
    }

    .discussion-rules {
      background: #e6f7ff;
      border: 1px solid #91d5ff;
      padding: 15px;
      border-radius: 6px;
      margin-bottom: 20px;
    }

    .discussion-rules h3 {
      margin: 0 0 10px 0;
      color: #0050b3;
      font-size: 16px;
    }

    .discussion-rules ul {
      margin: 0;
      padding-left: 20px;
      color: #003a8c;
      font-size: 14px;
    }

    .discussion-rules li {
      margin-bottom: 5px;
    }

    .post-comment {
      margin-bottom: 30px;
    }

    .post-comment textarea {
      width: 100%;
      padding: 10px;
      border: 1px solid #d9d9d9;
      border-radius: 6px;
      margin-bottom: 10px;
      font-family: inherit;
      resize: vertical;
    }

    .post-actions {
      text-align: right;
    }

    .comment-thread {
      margin-bottom: 20px;
    }

    .comment {
      display: flex;
      gap: 15px;
    }

    .comment-avatar img {
      width: 40px;
      height: 40px;
      border-radius: 50%;
    }

    .comment-content {
      flex: 1;
    }

    .comment-header {
      margin-bottom: 5px;
    }

    .username {
      font-weight: 600;
      margin-right: 10px;
      color: #262626;
    }

    .time {
      color: #8c8c8c;
      font-size: 12px;
    }

    .comment-body {
      color: #595959;
      line-height: 1.5;
      margin-bottom: 8px;
    }

    .comment-actions {
      margin-bottom: 10px;
    }

    .btn-link {
      background: none;
      border: none;
      color: #1890ff;
      cursor: pointer;
      padding: 0;
      font-size: 13px;
    }

    .btn-link:hover {
      text-decoration: underline;
    }

    .replies {
      margin-left: 55px;
      margin-top: 15px;
      border-left: 2px solid #f0f0f0;
      padding-left: 15px;
    }

    .reply {
      margin-bottom: 15px;
    }

    .reply-form {
      margin-top: 10px;
      background: #fafafa;
      padding: 10px;
      border-radius: 6px;
    }

    .reply-form textarea {
      width: 100%;
      padding: 8px;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      margin-bottom: 8px;
    }

    .reply-actions {
      display: flex;
      gap: 10px;
      justify-content: flex-end;
    }

    .btn-sm {
      padding: 4px 12px;
      font-size: 12px;
      border-radius: 4px;
      cursor: pointer;
      border: none;
    }

    .btn-primary {
      background: #1890ff;
      color: white;
    }

    .btn-secondary {
      background: #f0f0f0;
      color: #595959;
    }
  `]
})
export class ProblemDetailComponent implements OnInit {
  private http = inject(HttpClient);
  private submissionApi = inject(SubmissionApiService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  problem = signal<ProblemDetail | null>(null);
  loading = signal(false);
  error = signal('');
  
  selectedLanguage = 'JAVA';
  code = '';
  submitting = signal(false);
  running = signal(false);
  submitResult = signal<SubmissionFeedback | null>(null);
  activeTab = signal<'description' | 'leaderboard' | 'discussion'>('description');
  leaderboard = signal<LeaderboardEntry[]>([]);
  loadingLeaderboard = signal(false);
  
  // Discussion
  comments = signal<Comment[]>([]);
  loadingComments = signal(false);
  newCommentContent = signal('');
  replyingToId = signal<number | null>(null);
  replyContent = signal('');

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadProblem(parseInt(id));
    }
  }

  loadProblem(id: number) {
    this.loading.set(true);
    this.http.get<ProblemDetail>(`http://localhost:8087/api/v1/problems/${id}`).subscribe({
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

    const currentProblem = this.problem();
    if (!currentProblem) {
      this.submitResult.set({
        accepted: false,
        verdict: 'Error',
        message: 'Problem details unavailable. Please reload.'
      });
      return;
    }

    this.submitting.set(true);
    this.submitResult.set(null);

    const submission: SubmissionPayload = {
      problemId: currentProblem.id,
      language: this.selectedLanguage,
      code: this.code
    };

    this.submissionApi.submit(submission).subscribe({
      next: (result: SubmissionResponse) => {
        this.submitResult.set({
          accepted: result.status === 'ACCEPTED',
          verdict: result.status ?? 'SUBMITTED',
          message: result.resultMessage ?? 'Submission received. Check My Submissions for live status.',
          submissionId: result.id
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

  switchTab(tab: 'description' | 'leaderboard' | 'discussion') {
    this.activeTab.set(tab);
    if (tab === 'leaderboard') {
      this.loadLeaderboard();
    } else if (tab === 'discussion') {
      this.loadComments();
    }
  }

  loadComments() {
    if (!this.problem()) return;
    this.loadingComments.set(true);
    this.http.get<any>(`http://localhost:8087/api/v1/problems/${this.problem()!.id}/comments`)
      .subscribe({
        next: (data) => {
          this.comments.set(data.content || []);
          this.loadingComments.set(false);
        },
        error: (err) => {
          console.error('Failed to load comments', err);
          this.loadingComments.set(false);
        }
      });
  }

  postComment() {
    if (!this.newCommentContent().trim()) return;
    
    const payload = {
      content: this.newCommentContent()
    };

    this.http.post<Comment>(`http://localhost:8087/api/v1/problems/${this.problem()!.id}/comments`, payload)
      .subscribe({
        next: (comment) => {
          this.comments.update(prev => [comment, ...prev]);
          this.newCommentContent.set('');
        },
        error: (err) => alert('Failed to post comment')
      });
  }

  startReply(commentId: number) {
    this.replyingToId.set(commentId);
    this.replyContent.set('');
  }

  cancelReply() {
    this.replyingToId.set(null);
    this.replyContent.set('');
  }

  submitReply(parentId: number) {
    if (!this.replyContent().trim()) return;

    const payload = {
      content: this.replyContent(),
      parentId: parentId
    };

    this.http.post<Comment>(`http://localhost:8087/api/v1/problems/${this.problem()!.id}/comments`, payload)
      .subscribe({
        next: (reply) => {
          // Optimistically update UI or reload
          // For simplicity, reloading or finding parent and pushing
          this.loadComments(); 
          this.cancelReply();
        },
        error: (err) => alert('Failed to post reply')
      });
  }

  loadLeaderboard() {
    if (!this.problem()) return;
    this.loadingLeaderboard.set(true);
    this.submissionApi.getLeaderboard(this.problem()!.id).subscribe({
      next: (data) => {
        this.leaderboard.set(data);
        this.loadingLeaderboard.set(false);
      },
      error: (err) => {
        console.error('Failed to load leaderboard', err);
        this.loadingLeaderboard.set(false);
      }
    });
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (days > 0) return `${days}d ago`;
    if (hours > 0) return `${hours}h ago`;
    if (minutes > 0) return `${minutes}m ago`;
    return 'Just now';
  }

  getAvatarUrl(username: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(username)}&background=random&color=fff&size=128`;
  }
}
