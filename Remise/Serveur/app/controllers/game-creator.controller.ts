import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import { GameCreatorService } from '../services/game/game-creator.service';
import Types from '../types';

@injectable()
export class GameCreatorController {
    public router: Router;

    public constructor(@inject(Types.GameCreatorService) private creatorServ: GameCreatorService) {
        this.configureRouter();
    }

    private configureRouter() {
        this.router = Router();

        this.router.get('/game/suggestion', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await this.creatorServ.getSuggestion());
        });

        this.router.post('/game/new', async (req: Request, res: Response, next: NextFunction) => {
            await this.creatorServ.createGame(req.body).catch(e => {
                res.status(500);
                res.json(e.message);
            });
            res.status(200).send();
        });
    }

}
