import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import { DatabaseService } from '../services/database.service';
import Types from '../types';
import * as pg from "pg";

@injectable()
export class AccountController {
    public router: Router;

    public constructor(@inject(Types.DatabaseService) private databaseService: DatabaseService) {
        this.configureRouter();
    }

    private configureRouter() {
        this.router = Router();
        this.router.get('/selectAccount', (req: Request, res: Response, next: NextFunction) => {
            this.databaseService.selectAccount().then((result: pg.QueryResult) => {
                const accounts: any[] = result.rows.map((row: any) => (
                    {
                        id:       row.noanimal,
                        username: row.username,
                        password: row.password,
                    }));
                
                res.json(accounts);
            }).catch((e: Error) => {
                console.log("error", e)
            });;
        });
    }
}
