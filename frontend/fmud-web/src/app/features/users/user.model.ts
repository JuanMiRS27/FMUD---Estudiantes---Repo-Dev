import { UserRole } from '../../core/models/role.model';

export interface ManagedUser {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserFormValue {
  name: string;
  email: string;
  password: string;
  role: UserRole;
  enabled: boolean;
}
