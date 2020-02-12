import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import { DatabaseService } from '../services/database.service';
import Types from '../types';
import * as pg from 'pg';
import { IChannelMessageReq, IChannelIds } from '../interfaces/chat';


@injectable()
export class ChatController {
    public router: Router;

    public constructor(@inject(Types.DatabaseService) private db: DatabaseService) {
        this.configureRouter();
    }

    private configureRouter() {
        this.router = Router();

        this.router.get('/messages/:id', async (req: Request, res: Response, next: NextFunction) => {
            this.db.getMessagesWithChannelId(req.params.id).then((result: pg.QueryResult) => {
                
                const messages: IChannelMessageReq[] = result.rows.map((row: any) => (
                    {
                        username: row.out_username,
                        content:  row.out_content,
                        time:     row.out_times,
                    }
                ));
            
                res.json(messages);
            }).catch((e: Error) => {
                console.error(e.stack);
            });
        });

        this.router.get('/channels/:username', async (req: Request, res: Response, next: NextFunction) => {
            this.db.getChannelsWithAccountName(req.params.username).then((result: pg.QueryResult) => {

                const channels: IChannelIds[] = result.rows.map((row: any) => (
                    {
                        id: row.out_id,
                    }
                ));

                res.json(channels);
            });
        });

    }
}
