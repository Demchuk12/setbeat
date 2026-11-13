import { ChangeDetectionStrategy, Component, computed, inject, type OnInit } from "@angular/core";
import { VdBadge, type VdBadgeVariant } from "@vadem/ui/badge";
import { VdButton } from "@vadem/ui/button";
import { VdEmptyState, VdEmptyStateDescription, VdEmptyStateTitle } from "@vadem/ui/empty-state";
import { SessionCard } from "./session-card";
import { SessionStore } from "./session.store";

@Component({
  selector: "app-session-list",
  imports: [SessionCard, VdBadge, VdButton, VdEmptyState, VdEmptyStateDescription, VdEmptyStateTitle],
  templateUrl: "./session-list.html",
  styleUrl: "./session-list.css",
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SessionList implements OnInit {
  protected readonly store = inject(SessionStore);

  protected readonly watchBadge = computed((): { variant: VdBadgeVariant; text: string } => {
    const watch = this.store.watch();
    switch (watch.kind) {
      case "connected":
        return { variant: "success", text: watch.name };
      case "unavailable":
        return { variant: "warning", text: "Немає Wear OS" };
      case "disconnected":
        return { variant: "neutral", text: "Годинник не поруч" };
    }
  });

  ngOnInit(): void {
    void this.store.init();
  }
}
