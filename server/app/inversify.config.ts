import { Container } from 'inversify';
import { Application } from './app';
import { Server } from './server';
import { DateController } from './controllers/date.controller';
import { AccountController } from './controllers/account.controller';
import { DateService } from './services/date.service';
import { DatabaseService } from './services/database.service';
import Types from './types';

const container: Container = new Container();

container.bind(Types.Server).to(Server);
container.bind(Types.Application).to(Application);

container.bind(Types.DateController).to(DateController);
container.bind(Types.AccountController).to(AccountController);

container.bind(Types.DateService).to(DateService);
container.bind(Types.DatabaseService).to(DatabaseService).inSingletonScope();

export { container };
