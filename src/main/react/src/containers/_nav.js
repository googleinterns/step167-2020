export default [
  {
    _tag: 'CSidebarNavItem',
    name: 'Popular',
    to: '/popular',
    icon: 'cil-arrow-thick-top',
  },
  {
    _tag: 'CSidebarNavItem',
    name: 'New',
    to: '/new',
    icon: 'cil-clock',
  },
  {
    _tag: 'CSidebarNavItem',
    name: 'Add Recipe',
    to: '/addrecipe',
    icon: 'cil-plus',
  },
  {
    _tag: 'CSidebarNavTitle',
    _children: ['Account']
  },
  {
    _tag: 'CSidebarNavItem',
    name: 'Log In',
    to: '/login',
    icon: 'cil-chevron-right',
  },
  {
    _tag: 'CSidebarNavItem',
    name: 'Sign Up',
    to: '/register',
    icon: 'cil-chevron-double-right',
  }
]

