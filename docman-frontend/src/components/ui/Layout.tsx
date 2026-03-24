'use client';

import { Layout, theme } from 'antd';
import Header from './Header';
import Sidebar from './Sidebar';

const { Content } = Layout;

interface LayoutProps {
  children: React.ReactNode;
}

export default function MainLayout({ children }: LayoutProps) {
  const { token } = theme.useToken();

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header />
      <Layout>
        <Sidebar />
        <Content
          style={{
            padding: 24,
            background: token.colorBgLayout,
            overflow: 'auto',
          }}
        >
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}
