'use client';

import { useState } from 'react';
import { Card, Tabs, Form, Input, Button, Switch, Select, Avatar, Upload, message, Space, Typography, Row, Col } from 'antd';
import {
  UserOutlined,
  LockOutlined,
  BellOutlined,
  GlobalOutlined,
  SaveOutlined,
} from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { useAuth } from '@/hooks/useAuth';
import { useAppStore } from '@/stores/appStore';
import authService from '@/services/auth';

const { Title, Text } = Typography;

export default function SettingsPage() {
  const { t, i18n } = useTranslation();
  const { user } = useAuth();
  const { theme, setTheme, language, setLanguage } = useAppStore();
  const [profileForm] = Form.useForm();
  const [passwordForm] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const handleProfileUpdate = async (values: any) => {
    setLoading(true);
    try {
      // Profile update logic would go here
      message.success(t('common.success'));
    } catch (error: any) {
      message.error(error.message || t('common.error'));
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChange = async (values: { oldPassword: string; newPassword: string }) => {
    setLoading(true);
    try {
      await authService.changePassword(values.oldPassword, values.newPassword);
      message.success(t('common.success'));
      passwordForm.resetFields();
    } catch (error: any) {
      message.error(error.message || t('common.error'));
    } finally {
      setLoading(false);
    }
  };

  const handleLanguageChange = (value: string) => {
    setLanguage(value);
    i18n.changeLanguage(value);
  };

  const handleThemeChange = (value: string) => {
    setTheme(value as any);
  };

  const tabItems = [
    {
      key: 'profile',
      label: (
        <span>
          <UserOutlined />
          {t('settings.profile')}
        </span>
      ),
      children: (
        <Card>
          <Form
            form={profileForm}
            layout="vertical"
            initialValues={{
              username: user?.username,
              email: user?.email,
            }}
            onFinish={handleProfileUpdate}
          >
            <Row gutter={24}>
              <Col span={24}>
                <div style={{ textAlign: 'center', marginBottom: 24 }}>
                  <Avatar size={80} src={user?.avatar} icon={<UserOutlined />} />
                  <div style={{ marginTop: 16 }}>
                    <Upload showUploadList={false}>
                      <Button size="small">Change Avatar</Button>
                    </Upload>
                  </div>
                </div>
              </Col>
            </Row>

            <Row gutter={24}>
              <Col xs={24} md={12}>
                <Form.Item name="username" label={t('common.username')}>
                  <Input disabled />
                </Form.Item>
              </Col>
              <Col xs={24} md={12}>
                <Form.Item name="email" label={t('common.email')}>
                  <Input />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
              <Button type="primary" htmlType="submit" loading={loading} icon={<SaveOutlined />}>
                {t('common.save')}
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: 'security',
      label: (
        <span>
          <LockOutlined />
          {t('settings.security')}
        </span>
      ),
      children: (
        <Card title={t('settings.changePassword')}>
          <Form
            form={passwordForm}
            layout="vertical"
            onFinish={handlePasswordChange}
          >
            <Form.Item
              name="oldPassword"
              label={t('settings.oldPassword')}
              rules={[{ required: true, message: 'Please enter old password' }]}
            >
              <Input.Password />
            </Form.Item>

            <Form.Item
              name="newPassword"
              label={t('settings.newPassword')}
              rules={[{ required: true, message: 'Please enter new password' }]}
            >
              <Input.Password />
            </Form.Item>

            <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
              <Button type="primary" htmlType="submit" loading={loading}>
                {t('settings.changePassword')}
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: 'appearance',
      label: (
        <span>
          <GlobalOutlined />
          {t('settings.appearance')}
        </span>
      ),
      children: (
        <Card>
          <Form layout="vertical">
            <Form.Item label={t('settings.theme')}>
              <Select
                value={theme}
                onChange={handleThemeChange}
                options={[
                  { label: t('settings.light'), value: 'light' },
                  { label: t('settings.dark'), value: 'dark' },
                  { label: t('settings.system'), value: 'system' },
                ]}
              />
            </Form.Item>

            <Form.Item label={t('settings.language')}>
              <Select
                value={language}
                onChange={handleLanguageChange}
                options={[
                  { label: '中文', value: 'zh' },
                  { label: 'English', value: 'en' },
                ]}
              />
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: 'notifications',
      label: (
        <span>
          <BellOutlined />
          {t('settings.notifications')}
        </span>
      ),
      children: (
        <Card>
          <Form layout="vertical">
            <Form.Item
              label="Email Notifications"
              extra="Receive email notifications for document updates"
            >
              <Switch defaultChecked />
            </Form.Item>

            <Form.Item
              label="Desktop Notifications"
              extra="Receive desktop notifications for important updates"
            >
              <Switch defaultChecked />
            </Form.Item>
          </Form>
        </Card>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>{t('settings.title')}</Title>
      </div>

      <Tabs items={tabItems} />
    </div>
  );
}
