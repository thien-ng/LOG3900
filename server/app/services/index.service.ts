import { inject, injectable } from 'inversify';
import 'reflect-metadata';
import Types from '../types';
import { DateService } from './date.service';

@injectable()
export class IndexService {
    constructor(@inject(Types.DateService) private dateService: DateService) {}

    about(): any {
        return {
            title: 'This is merely a test',
            body: 'Lorem ipsum........',
        };
    }

    async helloWorld(): Promise<any> {
        return this.dateService
            .currentTime()
            .then((timeMessage: any) => {
                return {
                    title: 'Hello world',
                    body: 'Time is ' + timeMessage.body,
                };
            })
            .catch((error: unknown) => {
                console.error('There was an error!!!', error);

                return {
                    title: 'Error',
                    body: error as string,
                };
            });
    }
}
