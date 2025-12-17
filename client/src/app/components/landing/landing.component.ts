import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

interface Problem {
  id: number;
  title: string;
  difficulty: 'Easy' | 'Medium' | 'Hard';
  category: string;
  acceptance: number;
  submissions: number;
}

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <!-- Hero Section -->
    <section
      class="bg-gradient-to-br from-slate-900 via-primary-900 to-accent-900 text-white py-20 md:py-32"
    >
      <div class="container-max">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
          <div class="space-y-6 animate-fade-in">
            <div class="space-y-2">
              <div
                class="inline-block px-4 py-2 bg-primary-500/20 border border-primary-400 rounded-full mb-4"
              >
                <span class="text-sm font-semibold text-primary-200"
                  >Master Algorithms & Data Structures</span
                >
              </div>
              <h1 class="text-5xl md:text-6xl font-bold leading-tight">
                Code, Judge,
                <span
                  class="bg-gradient-to-r from-primary-400 to-accent-400 bg-clip-text text-transparent"
                  >Conquer</span
                >
              </h1>
            </div>
            <p class="text-xl text-slate-300 leading-relaxed">
              Challenge yourself with our extensive collection of coding
              problems. Improve your programming skills and prepare for
              technical interviews.
            </p>
            <div class="flex flex-col sm:flex-row gap-4 pt-4">
              <a
                routerLink="/problems"
                class="btn-primary text-lg px-8 py-3 bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-700 hover:to-primary-600"
              >
                Start Coding
              </a>
              <button
                class="btn-ghost text-lg px-8 py-3 border border-slate-400 text-white hover:bg-white/10"
              >
                Learn More
              </button>
            </div>
          </div>
          <div class="hidden md:block animate-slide-up">
            <div class="relative">
              <div
                class="absolute inset-0 bg-gradient-to-br from-primary-500 to-accent-500 rounded-2xl opacity-10 blur-3xl"
              ></div>
              <div
                class="relative bg-slate-800/50 backdrop-blur-sm border border-slate-700 rounded-2xl p-8 space-y-6"
              >
                <div class="flex items-center justify-center gap-4">
                  <div class="text-4xl">💻</div>
                  <div class="text-4xl">⚡</div>
                  <div class="text-4xl">🏆</div>
                </div>
                <div class="text-center space-y-3">
                  <p class="text-slate-300 font-mono text-sm">
                    Solve • Learn • Compete
                  </p>
                  <p class="text-accent-400 font-semibold">
                    2500+ Coding Problems
                  </p>
                  <p class="text-slate-400 text-sm">
                    Master DSA & Prepare for Tech Interviews
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Stats -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-6 mt-20">
          <div
            class="bg-white/10 backdrop-blur-sm border border-white/20 rounded-lg p-6 text-center"
          >
            <div class="text-3xl md:text-4xl font-bold text-primary-400">
              2500+
            </div>
            <p class="text-slate-300 mt-2">Problems</p>
          </div>
          <div
            class="bg-white/10 backdrop-blur-sm border border-white/20 rounded-lg p-6 text-center"
          >
            <div class="text-3xl md:text-4xl font-bold text-accent-400">
              150K+
            </div>
            <p class="text-slate-300 mt-2">Users</p>
          </div>
          <div
            class="bg-white/10 backdrop-blur-sm border border-white/20 rounded-lg p-6 text-center"
          >
            <div class="text-3xl md:text-4xl font-bold text-success-400">
              10M+
            </div>
            <p class="text-slate-300 mt-2">Submissions</p>
          </div>
          <div
            class="bg-white/10 backdrop-blur-sm border border-white/20 rounded-lg p-6 text-center"
          >
            <div class="text-3xl md:text-4xl font-bold text-warning-400">
              99%
            </div>
            <p class="text-slate-300 mt-2">Uptime</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Featured Problems -->
    <section class="py-20">
      <div class="container-max">
        <div class="mb-12">
          <h2 class="text-4xl font-bold text-slate-900 mb-4">
            Featured Problems
          </h2>
          <p class="text-lg text-slate-600">
            Start with our most popular challenges
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            *ngFor="let problem of featuredProblems"
            class="card p-6 cursor-pointer hover:shadow-xl hover:-translate-y-1 transition-all duration-300"
            (click)="goToProblem(problem.id)"
          >
            <div class="flex items-start justify-between mb-4">
              <h3 class="text-lg font-bold text-slate-900 flex-1">
                {{ problem.title }}
              </h3>
              <span
                [ngClass]="getDifficultyClass(problem.difficulty)"
                class="badge text-xs font-bold px-3"
              >
                {{ problem.difficulty }}
              </span>
            </div>
            <p class="text-slate-600 text-sm mb-4">{{ problem.category }}</p>
            <div
              class="flex items-center justify-between pt-4 border-t border-slate-200"
            >
              <div class="text-sm">
                <span class="text-slate-600">{{ problem.acceptance }}% </span>
                <span class="text-slate-400">acceptance</span>
              </div>
              <div class="text-sm text-slate-600">
                {{ problem.submissions }} solved
              </div>
            </div>
          </div>
        </div>

        <div class="text-center mt-12">
          <a routerLink="/problems" class="btn-primary px-8 py-3 text-lg">
            Browse All Problems
          </a>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="py-20 bg-slate-100">
      <div class="container-max">
        <h2 class="text-4xl font-bold text-slate-900 text-center mb-12">
          Why Choose CodeJudge?
        </h2>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div
            class="bg-white rounded-lg p-8 shadow-sm border border-slate-200"
          >
            <div
              class="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center mb-4"
            >
              <span class="text-2xl">⚡</span>
            </div>
            <h3 class="text-xl font-bold text-slate-900 mb-3">
              Instant Feedback
            </h3>
            <p class="text-slate-600">
              Get immediate results with detailed test case feedback to
              understand your mistakes.
            </p>
          </div>

          <div
            class="bg-white rounded-lg p-8 shadow-sm border border-slate-200"
          >
            <div
              class="w-12 h-12 bg-accent-100 rounded-lg flex items-center justify-center mb-4"
            >
              <span class="text-2xl">🏆</span>
            </div>
            <h3 class="text-xl font-bold text-slate-900 mb-3">
              Compete & Learn
            </h3>
            <p class="text-slate-600">
              Join global competitions, view solutions from top coders, and
              improve your ranking.
            </p>
          </div>

          <div
            class="bg-white rounded-lg p-8 shadow-sm border border-slate-200"
          >
            <div
              class="w-12 h-12 bg-success-100 rounded-lg flex items-center justify-center mb-4"
            >
              <span class="text-2xl">📚</span>
            </div>
            <h3 class="text-xl font-bold text-slate-900 mb-3">Learn & Grow</h3>
            <p class="text-slate-600">
              Access detailed explanations, editorial solutions, and curated
              learning paths.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section
      class="py-20 bg-gradient-to-r from-primary-600 to-accent-600 text-white"
    >
      <div class="container-max text-center space-y-6">
        <h2 class="text-4xl md:text-5xl font-bold">Ready to Level Up?</h2>
        <p class="text-xl text-white/90 max-w-2xl mx-auto">
          Join thousands of developers improving their coding skills every day.
          Start solving problems now!
        </p>
        <a
          routerLink="/problems"
          class="btn-primary bg-white text-primary-600 hover:bg-slate-100 px-8 py-3 text-lg font-semibold inline-block"
        >
          Start Now
        </a>
      </div>
    </section>
  `,
})
export class LandingComponent {
  private router = inject(Router);
  private authService = inject(AuthService);

  isAuthenticated = this.authService.isAuthenticated;

  featuredProblems: Problem[] = [
    {
      id: 1,
      title: 'Two Sum',
      difficulty: 'Easy',
      category: 'Array & Hashing',
      acceptance: 47.8,
      submissions: 25403,
    },
    {
      id: 2,
      title: 'Longest Substring Without Repeating Characters',
      difficulty: 'Medium',
      category: 'Sliding Window',
      acceptance: 33.4,
      submissions: 18204,
    },
    {
      id: 3,
      title: 'Median of Two Sorted Arrays',
      difficulty: 'Hard',
      category: 'Array & Binary Search',
      acceptance: 27.3,
      submissions: 9821,
    },
    {
      id: 4,
      title: 'Valid Parentheses',
      difficulty: 'Easy',
      category: 'Stack',
      acceptance: 40.1,
      submissions: 16542,
    },
    {
      id: 5,
      title: 'Merge K Sorted Lists',
      difficulty: 'Hard',
      category: 'Linked List',
      acceptance: 35.2,
      submissions: 7634,
    },
    {
      id: 6,
      title: 'Word Ladder',
      difficulty: 'Hard',
      category: 'Graph & BFS',
      acceptance: 33.8,
      submissions: 5892,
    },
  ];

  async login() {
    await this.authService.login();
  }

  async register() {
    await this.authService.register();
  }

  goToProblems() {
    this.router.navigate(['/problems']);
  }

  goToProblem(id: number) {
    this.router.navigate(['/problems', id]);
  }

  getDifficultyClass(difficulty: string): string {
    const classes: Record<string, string> = {
      Easy: 'badge-success',
      Medium: 'badge-warning',
      Hard: 'badge-danger',
    };
    return classes[difficulty] || 'badge-primary';
  }
}
