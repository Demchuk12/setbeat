import { ChangeDetectionStrategy, Component, computed, input } from "@angular/core";
import { heartRateTrace, type SessionExport } from "@setbeat/analytics";
import { VdLineChart } from "@vadem/ui/chart";
import { formatBpm, formatDuration } from "../shared/format";

const POINTS = 90;

@Component({
  selector: "app-heart-rate-chart",
  imports: [VdLineChart],
  template: `
    @if (hasData()) {
      <vd-line-chart
        title="Пульс"
        description="Середнє за кожен відрізок тренування, уд/хв"
        categoryLabel="Час від початку"
        [categories]="categories()"
        [series]="[{ name: 'Пульс', values: values() }]"
        [format]="formatBpm"
      />
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeartRateChart {
  readonly session = input.required<SessionExport>();
  readonly endedAt = input.required<number | null>();

  private readonly range = computed(() => {
    const session = this.session();
    const to = this.endedAt() ?? session.samples.at(-1)?.timestamp ?? session.startedAt;
    return { from: session.startedAt, to };
  });

  protected readonly values = computed(() => {
    const { from, to } = this.range();
    return heartRateTrace(this.session().samples, from, to, POINTS);
  });

  protected readonly categories = computed(() => {
    const { from, to } = this.range();
    const step = (to - from) / POINTS;
    return this.values().map((_, i) => formatDuration(i * step));
  });

  protected readonly hasData = computed(() => this.values().some((v) => v !== null));
  protected readonly formatBpm = formatBpm;
}
