'use client';

import { useState } from 'react';
import { Button, Input, Space, Card, Row, Col, Modal, Form, message, Typography, Empty } from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  TagsOutlined,
} from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { useCategories } from '@/hooks/useDocument';
import { documentService } from '@/services/document';

const { Title, Text } = Typography;

export default function CategoriesPage() {
  const { t } = useTranslation();
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState<any>(null);
  const [form] = Form.useForm();
  const [editForm] = Form.useForm();

  const { categories, isLoading, mutate } = useCategories();

  const handleCreate = async (values: { name: string; description?: string }) => {
    try {
      await documentService.createCategory(values);
      message.success(t('common.success'));
      setCreateModalVisible(false);
      form.resetFields();
      mutate();
    } catch (error: any) {
      message.error(error.message || t('common.error'));
    }
  };

  const handleEdit = async (values: { name: string; description?: string }) => {
    if (!selectedCategory) return;

    try {
      await documentService.updateCategory(selectedCategory.id, values);
      message.success(t('common.success'));
      setEditModalVisible(false);
      setSelectedCategory(null);
      editForm.resetFields();
      mutate();
    } catch (error: any) {
      message.error(error.message || t('common.error'));
    }
  };

  const handleDelete = (category: any) => {
    Modal.confirm({
      title: t('common.confirm'),
      okText: t('common.yes'),
      cancelText: t('common.no'),
      onOk: async () => {
        try {
          await documentService.deleteCategory(category.id);
          message.success(t('common.success'));
          mutate();
        } catch (error: any) {
          message.error(error.message || t('common.error'));
        }
      },
    });
  };

  const openEditModal = (category: any) => {
    setSelectedCategory(category);
    editForm.setFieldsValue({ name: category.name, description: category.description });
    setEditModalVisible(true);
  };

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Title level={3} style={{ margin: 0 }}>{t('categories.title')}</Title>
          </Col>
          <Col>
            <Space>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setCreateModalVisible(true)}
              >
                {t('categories.createCategory')}
              </Button>
            </Space>
          </Col>
        </Row>
      </div>

      <Card>
        {categories.length === 0 && !isLoading ? (
          <Empty description={t('categories.noCategories')} />
        ) : (
          <Row gutter={[16, 16]}>
            {categories.map((category) => (
              <Col xs={24} sm={12} md={8} lg={6} key={category.id}>
                <Card
                  hoverable
                  cover={
                    <div
                      style={{
                        height: 100,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                      }}
                    >
                      <TagsOutlined style={{ fontSize: 48, color: '#fff' }} />
                    </div>
                  }
                  actions={[
                    <Button
                      key="edit"
                      type="text"
                      icon={<EditOutlined />}
                      onClick={() => openEditModal(category)}
                    >
                      {t('common.edit')}
                    </Button>,
                    <Button
                      key="delete"
                      type="text"
                      danger
                      icon={<DeleteOutlined />}
                      onClick={() => handleDelete(category)}
                    >
                      {t('common.delete')}
                    </Button>,
                  ]}
                >
                  <Card.Meta
                    title={category.name}
                    description={
                      <Space direction="vertical" size="small" style={{ width: '100%' }}>
                        <Text type="secondary">{category.description || '-'}</Text>
                        <Text style={{ fontSize: 12 }}>{category.documentCount} documents</Text>
                      </Space>
                    }
                  />
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </Card>

      <Modal
        title={t('categories.createCategory')}
        open={createModalVisible}
        onCancel={() => {
          setCreateModalVisible(false);
          form.resetFields();
        }}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="name"
            label={t('categories.categoryName')}
            rules={[{ required: true, message: 'Please enter category name' }]}
          >
            <Input placeholder={t('categories.categoryName')} />
          </Form.Item>

          <Form.Item name="description" label={t('categories.description')}>
            <Input.TextArea placeholder={t('categories.description')} rows={3} />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setCreateModalVisible(false)}>
                {t('common.cancel')}
              </Button>
              <Button type="primary" htmlType="submit">
                {t('common.create')}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={t('common.edit')}
        open={editModalVisible}
        onCancel={() => {
          setEditModalVisible(false);
          setSelectedCategory(null);
          editForm.resetFields();
        }}
        footer={null}
      >
        <Form form={editForm} layout="vertical" onFinish={handleEdit}>
          <Form.Item
            name="name"
            label={t('categories.categoryName')}
            rules={[{ required: true, message: 'Please enter category name' }]}
          >
            <Input placeholder={t('categories.categoryName')} />
          </Form.Item>

          <Form.Item name="description" label={t('categories.description')}>
            <Input.TextArea placeholder={t('categories.description')} rows={3} />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setEditModalVisible(false)}>
                {t('common.cancel')}
              </Button>
              <Button type="primary" htmlType="submit">
                {t('common.save')}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
