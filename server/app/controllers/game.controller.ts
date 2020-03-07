import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import { LobbyManagerService } from '../services/game/lobby-manager.service';
import { GameManagerService } from '../services/game/game-manager.service';
import { GameCardService } from '../services/game/game-card.service';

import Types from '../types';

@injectable()
export class GameController {
    public router: Router;

    public constructor(
        @inject(Types.LobbyManagerService) private lobbyServ: LobbyManagerService,
        @inject(Types.GameManagerService)  private gameMan:   GameManagerService,
        @inject(Types.GameCardService)     private cardServ: GameCardService) {
        this.configureRouter();
    }

    private configureRouter() {
        this.router = Router();

        this.router.post('/lobby/join', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.lobbyServ.join(req.body));
        });

        this.router.post('/lobby/leave', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.lobbyServ.leave(req.body));
        });

        this.router.get('/lobby/active/:gameID', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.lobbyServ.getActiveLobbies(req.params.gameID));
        });

        this.router.get('/cards', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.cardServ.getGameCards());
        });

        this.router.get('/start/:lobbyName', (req: Request, res: Response, next: NextFunction) => {
            this.gameMan.startGame(req.params.lobbyName);
            res.status(200).send();
        });
    }
}
