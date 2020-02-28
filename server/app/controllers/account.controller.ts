import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import { AccountService } from '../services/account.service';
import Types from '../types';
import { UserManagerService } from '../services/user-manager.service';
import { IinfoUser } from '../interfaces/communication';

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

        /**
         * @swagger
         * /account/register:
         *  post:
         *    description: Use to register a new user
         *    responses:
         *      '200':
         *          description: A successful response
         */
        this.router.post('/register', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await this.accountService.register(req.body));
        });

        /**
         * @swagger
         * /account/login:
         *  post:
         *    description: Use to log a user
         *    responses:
         *      '200':
         *          description: A successful response
         */
        this.router.post('/login', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await this.accountService.login(req.body));
        });

        /**
         * @swagger
         * /account/users/online:
         *  get:
         *    description: Use to get online users
         *    responses:
         *      '200':
         *          description: A successful response
         */
        this.router.get('/users/online', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.userService.getOnlineUsers());
        });

        this.router.get('/user/info', async (req: Request, res: Response, next: NextFunction) => {
            this.accountService.getUserInfo(req.query.username).then((user: IinfoUser) => {
                res.json(user);
            })
        });
    }
}
