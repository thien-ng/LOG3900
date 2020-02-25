import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import Types from '../types';
import { LobbyManagerService } from '../services/game/lobby-manager.service';

@injectable()
export class GameController {
    public router: Router;

    public constructor(@inject(Types.LobbyManagerService) private lobbyServ: LobbyManagerService) {
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

        this.router.get('/lobby/active', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.lobbyServ.getActiveLobbies());
        });
    }
}
