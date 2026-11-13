import { ChangeDetectionStrategy, Component, computed, input } from "@angular/core";
import type { SessionSummary } from "@setbeat/analytics";
import { VdStat } from "@vadem/ui/stat";
import { formatBpm, formatDuration, formatKilograms, formatNumber } from "../../shared/format";

@Component({
  selector: "app-session-stats",
  imports: [VdStat],
  template: `
    <section class="stats" aria-label="Підсумки тренування">
      <vd-stat size="sm" label="Тривалість" [value]="duration()" />
      <vd-stat size="sm" label="Підходи" [value]="sets()" />
      <vd-stat size="sm" label="Обсяг" [value]="volume()" />
      <vd-stat size="sm" label="Відновлення за 60 с" [value]="hrr60()" />
    </section>
  `,
  styles: `
    .stats {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: var(--vd-space-4);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SessionStats {
  readonly summary = input.required<SessionSummary>();

  protected readonly duration = computed(() => formatDuration(this.summary().durationMs));
  protected readonly sets = computed(() => formatNumber(this.summary().totals.sets));
  protected readonly volume = computed(() => formatKilograms(this.summary().totals.volume));
  protected readonly hrr60 = computed(() => {
    const value = this.summary().totals.averageHrr60;
    return value === null ? "—" : formatBpm(value);
  });
}
