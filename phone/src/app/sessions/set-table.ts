import { ChangeDetectionStrategy, Component, computed, input } from "@angular/core";
import type { SetMetrics } from "@setbeat/analytics";
import { VdBadge } from "@vadem/ui/badge";
import { VdTable, VdTableEmpty, VdTableScroll } from "@vadem/ui/table";
import { formatDecimal, formatDuration } from "../shared/format";

@Component({
  selector: "app-set-table",
  imports: [VdBadge, VdTable, VdTableEmpty, VdTableScroll],
  templateUrl: "./set-table.html",
  styleUrl: "./set-table.css",
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SetTable {
  readonly sets = input.required<SetMetrics[]>();

  protected readonly hasUnlogged = computed(() => this.sets().some((s) => !s.logged));


  protected load(set: SetMetrics): string {
    const work = `${formatDecimal(set.weight)} кг × ${set.reps}`;
    return set.rpe === null ? work : `${work}, RPE ${formatDecimal(set.rpe)}`;
  }

  protected hrr60(set: SetMetrics): string {
    const hrr = set.hrr[60];
    return hrr.status === "ok" ? formatDecimal(Math.round(hrr.value)) : "—";
  }

  protected readonly formatDuration = formatDuration;
  protected readonly formatDecimal = formatDecimal;
}
