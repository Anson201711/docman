'use client';

import { Layout, Menu, Avatar, Dropdown, Space, Typography, theme } from 'antd';
import {
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
  BellOutlined,
  GlobalOutlined,
} from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { useRouter, usePathname } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { useAppStore } from '@/stores/appStore';

const { Header: AntHeader } = Layout;
const { Text } = Typography;

export default function Header() {
  const { t, i18n } = useTranslation();
  const router = useRouter();
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const { theme: currentTheme, setTheme, language, setLanguage } = useAppStore();
  const { token } = theme.useToken();

  const handleLogout = async () => {
    await logout();
    router.push('/login');
  };

  const handleLanguageChange = () => {
    const newLang = language === 'zh' ? 'en' : 'zh';
    setLanguage(newLang);
    i18n.changeLanguage(newLang);
  };

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: t('settings.profile'),
      onClick: () => router.push('/settings'),
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: t('common.settings'),
      onClick: () => router.push('/settings'),
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: t('common.logout'),
      onClick: handleLogout,
    },
  ];

  return (
    <AntHeader
      style={{
        background: token.colorBgContainer,
        padding: '0 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        borderBottom: `1px solid ${token.colorBorderSecondary}`,
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center' }}>
        <Text strong style={{ fontSize: 18, marginRight: 40 }}>
          {t('common.appName')}
        </Text>
      </div>

      <Space size="middle">
        <GlobalOutlined
          style={{ fontSize: 18, cursor: 'pointer' }}
          onClick={handleLanguageChange}
          title={language === 'zh' ? 'English' : '中文'}
        />

        <BellOutlined style={{ fontSize: 18, cursor: 'pointer' }} />

        <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
          <Space style={{ cursor: 'pointer' }}>
            <Avatar
              src={user?.avatar}
              icon={<UserOutlined />}
              style={{ backgroundColor: token.colorPrimary }}
            />
            <Text>{user?.username || 'User'}</Text>
          </Space>
        </Dropdown>
      </Space>
    </AntHeader>
  );
}
