import { registerLocaleData } from "@angular/common";
import localeUk from "@angular/common/locales/uk";
import { type ApplicationConfig, LOCALE_ID, provideBrowserGlobalErrorListeners } from "@angular/core";
import { provideRouter, withComponentInputBinding } from "@angular/router";
import { provideVdLocale, vdLocaleUk } from "@vadem/ui/locale";
import { routes } from "./app.routes";

registerLocaleData(localeUk);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    { provide: LOCALE_ID, useValue: "uk" },
    provideVdLocale(vdLocaleUk),
    provideRouter(routes, withComponentInputBinding()),
  ],
};
