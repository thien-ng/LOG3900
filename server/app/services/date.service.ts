import { injectable } from 'inversify';
import 'reflect-metadata';

@injectable()
export class DateService {
    async currentTime(): Promise<any> {
        return {
            title: 'Time',
            body: new Date().toString(),
        };
    }
}
