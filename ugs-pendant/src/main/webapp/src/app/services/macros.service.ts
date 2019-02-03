import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { Macro } from '../model/macro';
import 'rxjs/add/operator/map'

@Injectable({
  providedIn: 'root'
})
export class MacrosService {
  constructor(private http:HttpClient) { }

  getMacroList():Observable<Macro[]> {
      return this.http.get<any[]>('/api/macros/getMacroList')
        .map(response => {
          return response.map(macroResponse => {
            let macro = new Macro();
            macro.name = macroResponse.name;
            macro.description = macroResponse.description;
            macro.gcode = macroResponse.gcode;
            return macro;
          });
        });
  }

  runMacro(macro:Macro) {
     console.log(macro);
  }
}
