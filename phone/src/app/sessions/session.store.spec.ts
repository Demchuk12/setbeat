import { TestBed } from "@angular/core/testing";
import { beforeEach, describe, expect, it } from "vitest";
import { WEAR_SYNC } from "../wear/wear-sync.token";
import { SessionStore } from "./session.store";
import { FakeWearSync, sessionExport, settle } from "./testing/fixtures";

describe("SessionStore", () => {
  let wear: FakeWearSync;
  let store: SessionStore;

  beforeEach(() => {
    wear = new FakeWearSync();
    TestBed.configureTestingModule({ providers: [{ provide: WEAR_SYNC, useValue: wear }] });
    store = TestBed.inject(SessionStore);
  });

  it("loads stored sessions as summaries and the connected watch", async () => {
    await store.init();

    expect(store.sessions().map((s) => [s.planName, s.totals.sets, s.totals.volume])).toEqual([["Plan", 2, 1025]]);
    expect(store.watch()).toEqual({ kind: "connected", name: "Galaxy Watch8" });
    expect(store.loaded()).toBe(true);
  });

  it("initializes once however many screens ask", async () => {
    await Promise.all([store.init(), store.init()]);
    await store.init();

    expect(wear.listCalls).toBe(1);
  });

  it("finds a session with its raw export", async () => {
    await store.init();

    expect(store.find("s1")?.session.samples).toHaveLength(241);
    expect(store.find("missing")).toBeUndefined();
  });

  it("reports a phone without Wear OS", async () => {
    wear.available = false;

    await store.init();
    await store.sync();

    expect(store.watch()).toEqual({ kind: "unavailable" });
    expect(store.message()).toBe("Нових сесій немає");
  });

  it("reloads sessions when the watch delivers a new one", async () => {
    await store.init();
    wear.stored = [sessionExport(), sessionExport("s2")];

    wear.listener?.({ id: "s2" });
    await settle();

    expect(store.sessions()).toHaveLength(2);
  });
});
