import { registerPlugin, WebPlugin, type PluginListenerHandle } from "@capacitor/core";

export interface StoredSession {
  id: string;
  startedAt: number;
  endedAt: number | null;
  receivedAt: number;
}

export interface WearNode {
  id: string;
  name: string;
  nearby: boolean;
}

export interface WearSyncPlugin {
  listSessions(): Promise<{ sessions: StoredSession[] }>;
  getSession(options: { id: string }): Promise<{ json: string }>;
  syncNow(): Promise<{ available: boolean; received: number }>;
  connectedNodes(): Promise<{ available: boolean; nodes: WearNode[] }>;
  addListener(event: "sessionReceived", listener: (event: { id: string }) => void): Promise<PluginListenerHandle>;
}

class WearSyncWeb extends WebPlugin implements Omit<WearSyncPlugin, "addListener"> {
  async listSessions() {
    return { sessions: [] };
  }

  async getSession(options: { id: string }): Promise<{ json: string }> {
    throw this.unavailable(`session ${options.id} is only available on the phone`);
  }

  async syncNow() {
    return { available: false, received: 0 };
  }

  async connectedNodes() {
    return { available: false, nodes: [] };
  }
}

export const WearSync = registerPlugin<WearSyncPlugin>("WearSync", { web: () => new WearSyncWeb() });
