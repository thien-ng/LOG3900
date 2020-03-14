import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import { GameCardService } from '../services/game/game-card.service';

import Types from '../types';

@injectable()
export class CardController {
    public router: Router;

    public constructor(@inject(Types.GameCardService) private cardServ: GameCardService) {
        this.configureRouter();
    }

    private configureRouter() {
        this.router = Router();

        this.router.get('/', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await this.cardServ.getGameCards());
        });

        this.router.delete('/delete/:gameName', async (req: Request, res: Response, next: NextFunction) => {
            this.cardServ.deleteCard(req.params.gameName).then(() => {
                res.status(200).send();
            });
        });

    }
}
