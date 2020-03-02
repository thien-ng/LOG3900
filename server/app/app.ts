import * as bodyParser from 'body-parser';
import * as cookieParser from 'cookie-parser';
import * as cors from 'cors';
import * as express from 'express';
import { inject, injectable } from 'inversify';
import * as logger from 'morgan';
import { DateController } from './controllers/date.controller';
import { AccountController } from './controllers/account.controller';
import { ChatController } from './controllers/chat.controller';
import Types from './types';
import { GameController } from './controllers/game.controller';
import { GameCreatorController } from './controllers/game-creator.controller';

import * as swaggerDoc from 'swagger-jsdoc';
import * as swaggerUI from 'swagger-ui-express';

const swaggerOptions = {
    swaggerDefinition: {
      info: {
        title: "Projet de Pinture",
        description: "Server Express",
        version: "1.0.0",
      }
    },
    apis: [
        process.cwd() + "/app/controllers/*.ts"
    ],
};

const swagDoc = swaggerDoc(swaggerOptions);

@injectable()
export class Application {
    private readonly internalError: number = 500;
    app: express.Application;

    constructor(
        @inject(Types.DateController) private dateController: DateController,
        @inject(Types.AccountController) private accountController: AccountController,
        @inject(Types.ChatController) private chatController: ChatController,
        @inject(Types.GameController) private gameController: GameController,
        @inject(Types.GameCreatorController) private gameCreatorController: GameCreatorController,
    ) {
        this.app = express();
    
        this.config();

        this.bindRoutes();
    }

    private config(): void {
        // Middlewares configuration
        this.app.use(logger('dev'));
        this.app.use(bodyParser.json());
        this.app.use(bodyParser.urlencoded({ extended: true }));
        this.app.use(cookieParser());
        this.app.use(cors());
        this.app.use("/api-docs", swaggerUI.serve, swaggerUI.setup(swagDoc));
    }

    bindRoutes(): void {
        // Notre application utilise le routeur de notre API `Index`
        this.app.use('/date', this.dateController.router);
        this.app.use('/account', this.accountController.router);
        this.app.use('/chat', this.chatController.router);
        this.app.use('/game', this.gameController.router);
        this.app.use('/creator', this.gameCreatorController.router);
        this.errorHandling();
    }

    private errorHandling(): void {
        // When previous handlers have not served a request: path wasn't found
        this.app.use((req: express.Request, res: express.Response, next: express.NextFunction) => {
            const err: Error = new Error('Not Found');
            next(err);
        });

        // development error handler
        // will print stacktrace
        if (this.app.get('env') === 'development') {
            // tslint:disable-next-line:no-any
            this.app.use((err: any, req: express.Request, res: express.Response, next: express.NextFunction) => {
                res.status(err.status || this.internalError);
                res.send({
                    message: err.message,
                    error: err,
                });
            });
        }

        // production error handler
        // no stacktraces leaked to user (in production env only)
        // tslint:disable-next-line:no-any
        this.app.use((err: any, req: express.Request, res: express.Response, next: express.NextFunction) => {
            res.status(err.status || this.internalError);
            res.send({
                message: err.message,
                error: {},
            });
        });
    }
}
