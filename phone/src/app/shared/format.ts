const number = new Intl.NumberFormat("uk-UA", { maximumFractionDigits: 0 });
const decimal = new Intl.NumberFormat("uk-UA", { maximumFractionDigits: 1 });

export function formatDuration(ms: number | null): string {
  if (ms === null) return "—";
  const total = Math.round(ms / 1000);
  const hours = Math.floor(total / 3600);
  const minutes = Math.floor((total % 3600) / 60);
  const seconds = total % 60;
  const mmss = `${String(minutes).padStart(hours ? 2 : 1, "0")}:${String(seconds).padStart(2, "0")}`;
  return hours ? `${hours}:${mmss}` : mmss;
}

export function formatNumber(value: number | null): string {
  return value === null ? "—" : number.format(value);
}

export function formatKilograms(value: number): string {
  return `${decimal.format(value)} кг`;
}

export function formatDecimal(value: number | null): string {
  return value === null ? "—" : decimal.format(value);
}

export function formatBpm(value: number): string {
  return `${number.format(value)} уд/хв`;
}

export function formatPercent(share: number): string {
  return `${Math.round(share * 100)}%`;
}
