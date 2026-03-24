'use client';

import { useState } from 'react';
import { Form, Input, Button, Card, message, Typography, Space } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/hooks/useAuth';

const { Title, Text, Link: AntLink } = Typography;

export default function LoginPage() {
  const { t } = useTranslation();
  const router = useRouter();
  const { login, isLoading } = useAuth();
  const [form] = Form.useForm();

  const handleSubmit = async (values: { username: string; password: string }) => {
    try {
      await login(values.username, values.password);
      message.success(t('auth.loginSuccess'));
      router.push('/documents');
    } catch (error: any) {
      message.error(error.message || t('auth.invalidCredentials'));
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      }}
    >
      <Card style={{ width: 400, boxShadow: '0 14px 45px rgba(0,0,0,0.2)' }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <Title level={3}>{t('auth.loginTitle')}</Title>
        </div>

        <Form
          form={form}
          name="login"
          onFinish={handleSubmit}
          layout="vertical"
          size="large"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: t('auth.usernameRequired') }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder={t('common.username')}
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: t('auth.passwordRequired') }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder={t('common.password')}
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              block
              loading={isLoading}
            >
              {t('common.login')}
            </Button>
          </Form.Item>

          <div style={{ textAlign: 'center' }}>
            <Space>
              <Text>{t('common.noAccount')}</Text>
              <Link href="/register">{t('common.register')}</Link>
            </Space>
          </div>
        </Form>
      </Card>
    </div>
  );
}
