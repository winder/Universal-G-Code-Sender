import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { timer, interval } from 'rxjs';
import { switchMap, tap, retryWhen, delayWhen } from 'rxjs/operators';
import 'rxjs/add/operator/map'

import { Settings } from '../model/settings';

@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  private settingsSubject:Subject<Settings> = new Subject<Settings>();

  constructor(private http:HttpClient) { }

  /**
   * Starts a timer and refreshes the settings with event intervals
   */
  start() {
    interval(5000)
    .pipe(
      switchMap(_ => this.refreshSettings()),
      retryWhen(errors =>
        // Retry on errors
        errors.pipe(
          // On error send an invalid settings
          tap(error => {
            let settings = new Settings();
            this.settingsSubject.next(settings);
          }),
          // Restart in 5 seconds
          delayWhen(error => timer(10000))
        )
      )
    )
    .subscribe();
  }

  getSettings():Subject<Settings> {
    return this.settingsSubject;
  }

  refreshSettings():Observable<Settings> {
    return this.http.get<any>('/api/v1/settings/getSettings')
      .map(response => {
        let settings = new Settings();
        settings.jogFeedRate = response.jogFeedRate;
        settings.jogStepSizeXY = response.jogStepSizeXY;
        settings.jogStepSizeZ = response.jogStepSizeZ;
        settings.preferredUnits = response.preferredUnits;
        return settings;
      })
      .pipe(
        tap(settings => this.settingsSubject.next(settings))
      );
  }

  setSettings(settings:Settings):Observable<any> {
    let object = {
      jogFeedRate: settings.jogFeedRate,
      jogStepSizeXY: settings.jogStepSizeXY,
      jogStepSizeZ: settings.jogStepSizeZ,
      preferredUnits: settings.preferredUnits
    };

    return this.http.post('/api/v1/settings/setSettings', object).pipe(
      tap(() => this.refreshSettings().subscribe())
    );
  }
}
