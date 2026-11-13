import { TestBed } from "@angular/core/testing";
import { provideRouter } from "@angular/router";
import { RouterTestingHarness } from "@angular/router/testing";
import { describe, expect, it } from "vitest";
import { routes } from "./app.routes";

describe("App", () => {
  it("opens the session list with an empty state when there is no phone bridge", async () => {
    TestBed.configureTestingModule({ providers: [provideRouter(routes)] });
    const harness = await RouterTestingHarness.create("/");
    await new Promise((resolve) => setTimeout(resolve));
    harness.detectChanges();

    const text = harness.routeNativeElement?.textContent ?? "";
    expect(text).toContain("Немає Wear OS");
    expect(text).toContain("Тут з'являться твої тренування");
  });
});
