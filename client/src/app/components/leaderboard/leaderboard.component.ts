import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SubmissionApiService, GlobalLeaderboardEntry } from '../../services/submission-api.service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-leaderboard',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './leaderboard.component.html',
  styleUrls: ['./leaderboard.component.scss']
})
export class LeaderboardComponent implements OnInit {
  private submissionApi = inject(SubmissionApiService);
  private router = inject(Router);
  
  leaderboard = signal<GlobalLeaderboardEntry[]>([]);
  loading = signal<boolean>(true);

  ngOnInit() {
    this.loadLeaderboard();
  }

  loadLeaderboard() {
    this.loading.set(true);
    this.submissionApi.getGlobalLeaderboard().subscribe({
      next: (data) => {
        this.leaderboard.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to load leaderboard', err);
        this.loading.set(false);
      }
    });
  }
}
