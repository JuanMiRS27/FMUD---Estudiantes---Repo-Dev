export type UserRole = 'SECRETARIO' | 'ADMIN';

export function roleDisplayName(role: UserRole): string {
  return role === 'ADMIN' ? 'Junta Administrativa' : 'Secretaria';
}
