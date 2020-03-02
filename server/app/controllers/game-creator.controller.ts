import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import Types from '../types';

@injectable()
export class GameCreatorController {
    public router: Router;

    public constructor() {
        this.configureRouter();
    }

    private configureRouter() {
        this.router = Router();

        this.router.post('/game', (req: Request, res: Response, next: NextFunction) => {
            res.json();
        });
    }
}
