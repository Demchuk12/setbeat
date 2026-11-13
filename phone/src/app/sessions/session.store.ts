import { Injectable, computed, inject, signal } from "@angular/core";
import { summarizeSession, type SessionExport, type SessionSummary } from "@setbeat/analytics";
import type { WearNode } from "../wear/wear-sync.plugin";
import { WEAR_SYNC } from "../wear/wear-sync.token";

export type WatchStatus = { kind: "unavailable" } | { kind: "disconnected" } | { kind: "connected"; name: string };

export interface SessionRecord {
  session: SessionExport;
  summary: SessionSummary;
}

@Injectable({ providedIn: "root" })
export class SessionStore {
  private readonly wear = inject(WEAR_SYNC);
  private initialized: Promise<void> | null = null;

  private readonly _records = signal<SessionRecord[]>([]);
  private readonly _loaded = signal(false);
  private readonly _watch = signal<WatchStatus>({ kind: "disconnected" });
  private readonly _syncing = signal(false);
  private readonly _message = signal<string | null>(null);

  readonly records = this._records.asReadonly();
  readonly sessions = computed(() => this._records().map((r) => r.summary));
  readonly loaded = this._loaded.asReadonly();
  readonly watch = this._watch.asReadonly();
  readonly syncing = this._syncing.asReadonly();
  readonly message = this._message.asReadonly();

  init(): Promise<void> {
    this.initialized ??= this.start();
    return this.initialized;
  }

  find(id: string): SessionRecord | undefined {
    return this._records().find((r) => r.session.id === id);
  }

  async sync(): Promise<void> {
    this._syncing.set(true);
    try {
      const { available, received } = await this.wear.syncNow();
      if (!available) this._watch.set({ kind: "unavailable" });
      this._message.set(received ? `Отримано сесій: ${received}` : "Нових сесій немає");
      await Promise.all([this.loadSessions(), this.loadWatch()]);
    } catch (e) {
      this._message.set(`Помилка синхронізації: ${(e as Error).message}`);
    } finally {
      this._syncing.set(false);
    }
  }

  private async start(): Promise<void> {
    await this.wear.addListener("sessionReceived", () => void this.loadSessions());
    await Promise.all([this.loadSessions(), this.loadWatch()]);
  }

  private async loadSessions(): Promise<void> {
    const { sessions } = await this.wear.listSessions();
    const records = await Promise.all(
      sessions.map(async ({ id }) => {
        const { json } = await this.wear.getSession({ id });
        const session = JSON.parse(json) as SessionExport;
        return { session, summary: summarizeSession(session) };
      }),
    );
    this._records.set(records);
    this._loaded.set(true);
  }

  private async loadWatch(): Promise<void> {
    const { available, nodes } = await this.wear.connectedNodes();
    this._watch.set(toWatchStatus(available, nodes));
  }
}

function toWatchStatus(available: boolean, nodes: WearNode[]): WatchStatus {
  if (!available) return { kind: "unavailable" };
  const [watch] = nodes;
  return watch ? { kind: "connected", name: watch.name } : { kind: "disconnected" };
}
