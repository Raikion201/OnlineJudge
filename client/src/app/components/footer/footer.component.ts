import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <footer class="bg-slate-900 text-slate-300 border-t border-slate-800">
      <div class="container-max py-12">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
          <!-- Brand -->
          <div>
            <div class="flex items-center gap-2 mb-4">
              <div
                class="w-8 h-8 bg-gradient-to-br from-primary-500 to-accent-500 rounded-lg flex items-center justify-center"
              >
                <span class="text-white font-bold text-lg">&lt;/&gt;</span>
              </div>
              <span class="text-lg font-bold text-white">CodeJudge</span>
            </div>
            <p class="text-sm text-slate-400">
              Master your coding skills with our comprehensive online judge
              platform.
            </p>
          </div>

          <!-- Links -->
          <div>
            <h4 class="text-white font-semibold mb-4">Platform</h4>
            <ul class="space-y-2">
              <li>
                <a
                  href="#"
                  class="text-slate-400 hover:text-white transition-colors"
                  >Problems</a
                >
              </li>
              <li>
                <a
                  href="#"
                  class="text-slate-400 hover:text-white transition-colors"
                  >Leaderboard</a
                >
              </li>
              <li>
                <a
                  href="#"
                  class="text-slate-400 hover:text-white transition-colors"
                  >Submissions</a
                >
              </li>
            </ul>
          </div>

          <!-- Resources -->
          <div>
            <h4 class="text-white font-semibold mb-4">Resources</h4>
            <ul class="space-y-2">
              <li>
                <a
                  href="#"
                  class="text-slate-400 hover:text-white transition-colors"
                  >Documentation</a
                >
              </li>
              <li>
                <a
                  href="#"
                  class="text-slate-400 hover:text-white transition-colors"
                  >API</a
                >
              </li>
              <li>
                <a
                  href="#"
                  class="text-slate-400 hover:text-white transition-colors"
                  >Blog</a
                >
              </li>
            </ul>
          </div>

          <!-- Legal -->
          <div>
            <h4 class="text-white font-semibold mb-4">Company</h4>
            <ul class="space-y-2">
              <li>
                <a
                  href="#"
                  class="text-slate-400 hover:text-white transition-colors"
                  >About</a
                >
              </li>
              <li>
                <a
                  href="#"
                  class="text-slate-400 hover:text-white transition-colors"
                  >Privacy</a
                >
              </li>
              <li>
                <a
                  href="#"
                  class="text-slate-400 hover:text-white transition-colors"
                  >Terms</a
                >
              </li>
            </ul>
          </div>
        </div>

        <!-- Bottom -->
        <div
          class="border-t border-slate-800 pt-8 flex flex-col md:flex-row items-center justify-between"
        >
          <p class="text-sm text-slate-400">
            &copy; 2024 CodeJudge. All rights reserved.
          </p>
          <div class="flex items-center gap-4 mt-4 md:mt-0">
            <a
              href="#"
              class="text-slate-400 hover:text-white transition-colors"
              >Twitter</a
            >
            <a
              href="#"
              class="text-slate-400 hover:text-white transition-colors"
              >GitHub</a
            >
            <a
              href="#"
              class="text-slate-400 hover:text-white transition-colors"
              >Discord</a
            >
          </div>
        </div>
      </div>
    </footer>
  `,
})
export class FooterComponent {}

