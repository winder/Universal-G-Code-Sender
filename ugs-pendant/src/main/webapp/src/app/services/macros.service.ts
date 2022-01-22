import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {map} from 'rxjs/operators';

import {Macro} from '../model/macro';

@Injectable({
    providedIn: 'root'
})
export class MacrosService {
    constructor(private http: HttpClient) {
    }

    getMacroList(): Observable<Macro[]> {
        return this.http.get<any[]>('/api/v1/macros/getMacroList')
            .pipe(map(response => {
                return response.map(macroResponse => {
                    let macro = new Macro();
                    macro.name = macroResponse.name;
                    macro.description = macroResponse.description;
                    macro.gcode = macroResponse.gcode;
                    return macro;
                });
            }));
    }

    runMacro(macro: Macro) {
        let data = {
            gcode: macro.gcode,
            name: macro.name,
            description: macro.description
        };

        return this.http.post<any[]>('/api/v1/macros/runMacro', data);
    }
}
