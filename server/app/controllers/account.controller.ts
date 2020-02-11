import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import { AccountService } from '../services/account.service';
import Types from '../types';

@injectable()
export class AccountController {
    public router: Router;

    public constructor(@inject(Types.AccountService) private accountService: AccountService) {
        this.configureRouter();
    }

    private configureRouter() {
        this.router = Router();

        this.router.post('/register', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await this.accountService.register(req.body));
        });

        this.router.post('/login', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await this.accountService.login(req.body));
        });

        this.router.get('/users/online', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.accountService.getOnlineUsers());
        });
    }
}
