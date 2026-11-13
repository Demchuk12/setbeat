import { ChangeDetectionStrategy, Component, computed, input } from "@angular/core";
import { heartRateTrace, type SessionExport, type SetMetrics } from "@setbeat/analytics";
import { VdLineChart } from "@vadem/ui/chart";
import { formatBpm, formatDuration } from "../../shared/format";
import { workoutRanges } from "./workout-ranges";

const POINTS = 90;

@Component({
  selector: "app-heart-rate-chart",
  imports: [VdLineChart],
  template: `
    @if (hasData()) {
      <vd-line-chart
        class="heart-rate"
        title="Пульс"
        description="Підходи і відпочинок підсвічені, уд/хв"
        categoryLabel="Час від початку"
        rangeLabel="Етап"
        [zero]="false"
        [categories]="categories()"
        [series]="[{ name: 'Пульс', values: values() }]"
        [ranges]="ranges()"
        [format]="formatBpm"
      />
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeartRateChart {
  readonly session = input.required<SessionExport>();
  readonly endedAt = input.required<number | null>();
  readonly sets = input<readonly SetMetrics[]>([]);

  private readonly timeline = computed(() => {
    const session = this.session();
    const to = this.endedAt() ?? session.samples.at(-1)?.timestamp ?? session.startedAt;
    return { from: session.startedAt, to, points: POINTS };
  });

  protected readonly values = computed(() => {
    const { from, to, points } = this.timeline();
    return heartRateTrace(this.session().samples, from, to, points);
  });

  protected readonly categories = computed(() => {
    const { from, to, points } = this.timeline();
    const step = (to - from) / points;
    return this.values().map((_, i) => formatDuration(i * step));
  });

  protected readonly ranges = computed(() => workoutRanges(this.sets(), this.timeline()));
  protected readonly hasData = computed(() => this.values().some((v) => v !== null));
  protected readonly formatBpm = formatBpm;
}
