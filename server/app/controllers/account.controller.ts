import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import { AccountService } from '../services/account.service';
import Types from '../types';
import { UserManagerService } from '../services/user-manager.service';

@injectable()
export class AccountController {
    public router: Router;

    public constructor(
        @inject(Types.AccountService) private accountService: AccountService,
        @inject(Types.UserManagerService) private userService: UserManagerService) {
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
            res.json(this.userService.getOnlineUsers());
        });
    }
}
