import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import { DatabaseService } from '../services/database.service';
import Types from '../types';


@injectable()
export class ChatController {
    public router: Router;

    public constructor(@inject(Types.DatabaseService) private db: DatabaseService) {
        this.configureRouter();
    }

    private configureRouter() {
        this.router = Router();

        this.router.get('/messages/:id', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await (await this.db.getMessagesWithChannelId(Number(req.params.id))).rows);
        });

    }
}
