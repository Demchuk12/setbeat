import { DatePipe } from "@angular/common";
import { ChangeDetectionStrategy, Component, computed, input } from "@angular/core";
import { RouterLink } from "@angular/router";
import type { SessionSummary } from "@setbeat/analytics";
import { VdBadge } from "@vadem/ui/badge";
import { VdCard, VdCardDescription, VdCardFooter, VdCardHeader, VdCardLink, VdCardTitle } from "@vadem/ui/card";
import { VdSparkline } from "@vadem/ui/chart";
import { formatBpm, formatPercent } from "../../shared/format";
import { SessionStats } from "../session-stats/session-stats";

const RELIABLE_SHARE_WARNING = 0.8;

@Component({
  selector: "app-session-card",
  imports: [
    DatePipe,
    RouterLink,
    SessionStats,
    VdBadge,
    VdCard,
    VdCardDescription,
    VdCardFooter,
    VdCardHeader,
    VdCardLink,
    VdCardTitle,
    VdSparkline,
  ],
  templateUrl: "./session-card.html",
  styleUrl: "./session-card.css",
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SessionCard {
  readonly session = input.required<SessionSummary>();

  protected readonly hasTrace = computed(() => this.session().heartRateTrace.some((v) => v !== null));
  protected readonly unreliableHeartRate = computed(() => this.session().quality.reliableShare < RELIABLE_SHARE_WARNING);

  protected readonly formatBpm = formatBpm;
  protected readonly formatPercent = formatPercent;
}
