import type { Routes } from "@angular/router";

export const routes: Routes = [
  { path: "", loadComponent: () => import("./sessions/session-list").then((m) => m.SessionList), title: "Тренування" },
  {
    path: "sessions/:id",
    loadComponent: () => import("./sessions/session-detail").then((m) => m.SessionDetail),
    title: "Тренування",
  },
  { path: "**", redirectTo: "" },
];
