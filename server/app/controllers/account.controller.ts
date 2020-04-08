import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import { AccountService } from '../services/account.service';
import Types from '../types';
import { UserManagerService } from '../services/user-manager.service';
import { IInfoUser } from '../interfaces/communication';

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
         * /account/setAvatar:
         *  post:
         *    description: Use to set or change the avatarof an existing user
         *    responses:
         *      '200':
         *          description: A successful response
         */
        this.router.post('/avatar', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await this.accountService.setAvatar(req.body));
        });

        /**
         * @swagger
         * /account/getAvatar:
         *  get:
         *    description: Use to get the avatar of an existing user
         *    responses:
         *      '200':
         *          description: A successful response
         */
        this.router.get('/avatar/:username', async (req: Request, res: Response, next: NextFunction) => {
            this.accountService.getAvatar(req.params.username).then((result: string) => {
                res.json(result);
            });
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
        this.router.get('/users/online/:word?', (req: Request, res: Response, next: NextFunction) => {
            res.json(this.userService.getOnlineUsers(req.params.word));
        });

        this.router.get('/user/info/:username', async (req: Request, res: Response, next: NextFunction) => {
            this.accountService.getUserInfo(req.params.username).then((user: IInfoUser) => {
                res.json(user);
            })
        });
    }
}
