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

        this.router.post('/game/new', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.creatorServ.createGame(req.body));
        });
    }

}
