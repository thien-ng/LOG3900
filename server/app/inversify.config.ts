import { Container } from 'inversify';
import { Application } from './app';
import { Server } from './server';
import { DateController } from './controllers/date.controller';
import { AccountController } from './controllers/account.controller';
import { GameController } from './controllers/game.controller';
import { DateService } from './services/date.service';
import { AccountService } from './services/account.service';
import { WebsocketService } from './services/websocket.service';
import { ChatService } from './services/chat.service';
import { UserManagerService } from './services/user-manager.service';
import { ChatController } from './controllers/chat.controller';
import { AccountDbService } from './database/account-db.service';
import { ChatDbService } from './database/chat-db.service';
import { DatabaseService } from './database/database';
import { LobbyManagerService } from './services/game/lobby-manager.service';
import Types from './types';

const container: Container = new Container();

container.bind(Types.Server).to(Server);
container.bind(Types.Application).to(Application);

container.bind(Types.DateController).to(DateController);
container.bind(Types.AccountController).to(AccountController);
container.bind(Types.ChatController).to(ChatController);
container.bind(Types.GameController).to(GameController);

container.bind(Types.DateService).to(DateService);
container.bind(Types.AccountService).to(AccountService);
container.bind(Types.WebsocketService).to(WebsocketService);
container.bind(Types.LobbyManagerService).to(LobbyManagerService);

container.bind(Types.ChatDbService).to(ChatDbService).inSingletonScope();
container.bind(Types.AccountDbService).to(AccountDbService).inSingletonScope();
container.bind(Types.ChatService).to(ChatService).inSingletonScope();
container.bind(Types.UserManagerService).to(UserManagerService).inSingletonScope();
container.bind(Types.DatabaseService).to(DatabaseService).inRequestScope();

export { container };
