import { InjectionToken } from "@angular/core";
import { WearSync, type WearSyncPlugin } from "./wear-sync.plugin";

const capacitorWearSync: WearSyncPlugin = {
  listSessions: () => WearSync.listSessions(),
  getSession: (options) => WearSync.getSession(options),
  syncNow: () => WearSync.syncNow(),
  connectedNodes: () => WearSync.connectedNodes(),
  addListener: (event, listener) => WearSync.addListener(event, listener),
};

export const WEAR_SYNC = new InjectionToken<WearSyncPlugin>("WearSync", {
  providedIn: "root",
  factory: () => capacitorWearSync,
});
