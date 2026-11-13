import { TestBed } from "@angular/core/testing";
import { provideRouter } from "@angular/router";
import { describe, expect, it } from "vitest";
import { WEAR_SYNC } from "../wear/wear-sync.token";
import { SessionDetail } from "./session-detail";
import { FakeWearSync, settle } from "./testing/fixtures";

async function render(id: string) {
  TestBed.configureTestingModule({ providers: [provideRouter([]), { provide: WEAR_SYNC, useValue: new FakeWearSync() }] });
  const fixture = TestBed.createComponent(SessionDetail);
  fixture.componentRef.setInput("id", id);
  await fixture.whenStable();
  await settle();
  await fixture.whenStable();
  return fixture.nativeElement as HTMLElement;
}

describe("SessionDetail", () => {
  it("shows the session with its sets and heart rate chart", async () => {
    const page = await render("s1");
    const rows = [...page.querySelectorAll("tbody tr")].map((r) => r.textContent?.replace(/\s+/g, " ").trim());

    expect(page.querySelector("h1")?.textContent).toContain("Plan");
    expect(page.querySelector("vd-line-chart")).not.toBeNull();
    expect(rows).toHaveLength(2);
    expect(rows[0]).toContain("Squat 1");
    expect(rows[0]).toContain("102,5 кг × 5");
    expect(rows[0]).toContain("102,5 кг × 5, RPE 8,5");
    expect(rows[1]).toContain("план");
  });

  it("explains when the session is missing", async () => {
    const page = await render("missing");

    expect(page.textContent).toContain("Тренування не знайдено");
  });
});
