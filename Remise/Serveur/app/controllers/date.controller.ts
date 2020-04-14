import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import { DateService } from '../services/date.service';
import Types from '../types';

@injectable()
export class DateController {
    router: Router;

    constructor(@inject(Types.DateService) private dateService: DateService) {
        this.configureRouter();
    }

    private configureRouter() {
        this.router = Router();
        this.router.get('/', (req: Request, res: Response, next: NextFunction) => {
            // Send the request to the service and send the response
            this.dateService
                .currentTime()
                .then((time: any) => {
                    res.json(time);
                });
        });
    }
}
