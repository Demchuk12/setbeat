import { DatePipe } from "@angular/common";
import { ChangeDetectionStrategy, Component, computed, inject, input, type OnInit } from "@angular/core";
import { RouterLink } from "@angular/router";
import { VdBadge } from "@vadem/ui/badge";
import { VdButton } from "@vadem/ui/button";
import { VdCard } from "@vadem/ui/card";
import { VdEmptyState, VdEmptyStateDescription, VdEmptyStateTitle } from "@vadem/ui/empty-state";
import { VdIcon } from "@vadem/ui/icon";
import { HeartRateChart } from "./heart-rate-chart";
import { SessionStats } from "./session-stats";
import { SessionStore } from "./session.store";
import { SetTable } from "./set-table";

@Component({
  selector: "app-session-detail",
  imports: [
    DatePipe,
    HeartRateChart,
    RouterLink,
    SessionStats,
    SetTable,
    VdBadge,
    VdButton,
    VdCard,
    VdEmptyState,
    VdEmptyStateDescription,
    VdEmptyStateTitle,
    VdIcon,
  ],
  templateUrl: "./session-detail.html",
  styleUrl: "./session-detail.css",
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SessionDetail implements OnInit {
  readonly id = input.required<string>();

  protected readonly store = inject(SessionStore);
  protected readonly record = computed(() => (this.store.records(), this.store.find(this.id())));

  ngOnInit(): void {
    void this.store.init();
  }
}
