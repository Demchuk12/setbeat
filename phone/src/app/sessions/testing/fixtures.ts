import type { SessionExport } from "@setbeat/analytics";
import type { WearSyncPlugin } from "../../wear/wear-sync.plugin";

const plan = { id: "p", name: "Plan", exercises: [{ name: "Squat", sets: 2, reps: 5, weight: 100, restSeconds: 90 }] };

function event(seq: number, at: number, payload: Record<string, unknown>) {
  return { id: `e${seq}`, seq, timestamp: at, deviceId: "w", schemaVersion: 1, payload: { ...payload, at } };
}

export function sessionExport(id = "s1"): SessionExport {
  const samples = Array.from({ length: 241 }, (_, t) => ({
    timestamp: t * 1000,
    bpm: t >= 10 && t <= 40 ? 100 + (t - 10) : t > 40 && t < 160 ? Math.max(90, 130 - 0.5 * (t - 40)) : 95,
    accuracy: "ACCURACY_HIGH",
  }));
  return {
    schemaVersion: 2,
    id,
    exerciseType: "STRENGTH_TRAINING",
    startedAt: 0,
    endedAt: 240_000,
    events: [
      event(0, 0, { type: "session_started", plan }),
      event(1, 10_000, { type: "set_started" }),
      event(2, 40_000, { type: "set_ended" }),
      event(3, 45_000, { type: "set_logged", weight: 102.5, reps: 5, rpe: 8.5 }),
      event(4, 160_000, { type: "set_started" }),
      event(5, 190_000, { type: "set_ended" }),
      event(6, 240_000, { type: "finished" }),
    ],
    samples,
  } as SessionExport;
}

export class FakeWearSync implements WearSyncPlugin {
  available = true;
  nodes = [{ id: "n1", name: "Galaxy Watch8", nearby: true }];
  stored: SessionExport[] = [sessionExport()];
  listener: ((event: { id: string }) => void) | null = null;
  listCalls = 0;

  async listSessions() {
    this.listCalls++;
    return { sessions: this.stored.map((s) => ({ id: s.id, startedAt: s.startedAt, endedAt: s.endedAt, receivedAt: 0 })) };
  }

  async getSession({ id }: { id: string }) {
    return { json: JSON.stringify(this.stored.find((s) => s.id === id)) };
  }

  async syncNow() {
    return { available: this.available, received: 0 };
  }

  async connectedNodes() {
    return { available: this.available, nodes: this.available ? this.nodes : [] };
  }

  async addListener(_: "sessionReceived", listener: (event: { id: string }) => void) {
    this.listener = listener;
    return { remove: async () => {} };
  }
}

export const settle = () => new Promise((resolve) => setTimeout(resolve));
