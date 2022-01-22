import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {map} from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class FilesService {

    constructor(private http: HttpClient) {
    }

    send(): Observable<any> {
        return this.http.post('/api/v1/files/send', null);
    }

    pause(): Observable<any> {
        return this.http.get('/api/v1/files/pause');
    }

    cancel(): Observable<any> {
        return this.http.get('/api/v1/files/cancel');
    }

    uploadAndOpen(file: File): Observable<any> {
        let formData: FormData = new FormData();
        formData.append('file', file, file.name);
        return this.http.post("/api/v1/files/uploadAndOpen", formData)
            .pipe(
                map((response: Response) => {
                    return response;
                }));
    }
}
